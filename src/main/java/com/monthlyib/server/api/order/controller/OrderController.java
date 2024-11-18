package com.monthlyib.server.api.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.order.dto.OrderRequestDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.order.dto.OrderDto;
import com.monthlyib.server.domain.order.dto.OrderErrorDto;
import com.monthlyib.server.domain.order.dto.OrderResponseDto;
import com.monthlyib.server.domain.order.service.IbOrderService;
import com.monthlyib.server.domain.subscribe.service.SubscribeService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.exception.ServiceLogicException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "L. Order", description = "주문 관리 API")
public class OrderController {

    private final IbOrderService ibOrderService;

    private final SubscribeService subscribeService;

    @Value("${TOSS_SECRET_KEY}")
    private String KEY;


    @Operation(summary = "결제 승인", description = "결제 승인 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")})
    })
    @PostMapping("/confirm")
    public ResponseEntity<ResponseDto<OrderResponseDto>> confirmPayment(
            @RequestBody OrderRequestDto requestDto,
            @Parameter(hidden = true) @UserSession User user
            ) {
        subscribeService.verifyActiveSubUserThrowError(user.getUserId());
        String orderId;
        String amount;
        String paymentKey;
        // 클라이언트에서 받은 JSON 요청 바디입니다.
        paymentKey = requestDto.getPaymentKey();
        orderId = requestDto.getOrderId();
        amount = requestDto.getAmount();
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = KEY;
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        try {
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            byte[] bytes = obj.toString().getBytes("UTF-8");
            outputStream.write(bytes);

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

            Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            if (isSuccess) {
                OrderDto orderDto = objectMapper.readValue(reader, OrderDto.class);
                log.info(orderDto.toString());
                OrderResponseDto response = ibOrderService.createIbOrder(orderDto, user.getUserId(), requestDto.getSubscribeId());
                responseStream.close();
                return ResponseEntity.status(code).body(ResponseDto.of(response, Result.ok()));
            } else {
                OrderErrorDto orderErrorDto = objectMapper.readValue(reader, OrderErrorDto.class);
                responseStream.close();
                return ResponseEntity.status(code).body(ResponseDto.of(null, Result.error(orderErrorDto.getMessage())));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "특정 회원의 결제 내역 조회", description = "특정 회원의 결제 내역 조회 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/list/{userId}")
    ResponseEntity<PageResponseDto<List<OrderResponseDto>>> getUserOrderList(
            @RequestParam(defaultValue = "0") int page,
            @UserSession @Parameter(hidden = true) User user,
            @PathVariable Long userId
    ) {
        if (user.getUserId() != userId || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED_REQUEST_API);
        }
        Page<OrderResponseDto> response = ibOrderService.findOrderByUserId(userId, page, 20);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Operation(summary = "특정 구독 상품의 결제 내역 조회", description = "특정 구독 상품의 결제 내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/sub/list/{subscribeId}")
    ResponseEntity<PageResponseDto<List<OrderResponseDto>>> getSubOrderList(
            @RequestParam(defaultValue = "0") int page,
            @UserSession @Parameter(hidden = true) User user,
            @PathVariable Long subscribeId
    ) {
        if (user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED_REQUEST_API);
        }
        Page<OrderResponseDto> response = ibOrderService.findOrderBySubscribeId(subscribeId, page, 20);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

}

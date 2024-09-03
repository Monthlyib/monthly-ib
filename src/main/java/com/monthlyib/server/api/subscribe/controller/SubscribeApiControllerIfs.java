package com.monthlyib.server.api.subscribe.controller;


import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeResponseDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "J. Subscribe", description = "구독 API")
public interface SubscribeApiControllerIfs {


    class SubscribeListResponse extends ResponseDto<List<SubscribeResponseDto>> {

    }

    class SubscribeResponse extends ResponseDto<SubscribeResponseDto> {

    }

    class SubscribeUserResponse extends ResponseDto<SubscribeUserResponseDto> {

    }
    class SubscribeUserPageResponse extends PageResponseDto<List<SubscribeUserResponseDto>> {

    }

    @Operation(summary = "구독 상품 조회 요청(개인, 관리자)", description = "구독 상품 Data 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeListResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> getSubscribe();

    @Operation(summary = "현재 구독중인 상품 조회(개인)", description = "사용자의 현재 구독중인 상품 조회, 상태 코드로 확인 가능, 회원의 과거 구독 이력까지 모두 응답됨")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeUserPageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<PageResponseDto<?>> getSubscribeUser(
            @RequestParam(defaultValue = "0") int page,
            @PathVariable @Parameter(description = "User 식별자", required = true) Long userId,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "현재 활성화 상태 구독 조회(개인)", description = "사용자의 현재 활성화 되어있는 구독 상품 조회, 활성화 상태 데이터 없으면 null, 있으면 단건 응답")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeUserPageResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<SubscribeUserResponseDto>> getActiveSubscribeUser(
            @PathVariable @Parameter(description = "User 식별자", required = true) Long userId,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "구독 상품 생성(관리자)", description = "구독 상품 생성 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postSubscribe(
            @RequestBody SubscribePostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "구독 생성 및 요청(관리자)", description = "구독 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeUserResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> postSubscribeUser(
            @PathVariable @Parameter(description = "Subscribe 식별자", required = true) Long subscribeId,
            @PathVariable @Parameter(description = "회원 식별자", required = true) Long userId,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "구독 상품 수정(관리자)", description = "구독 상품 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchSubscribe(
            @PathVariable @Parameter(description = "Subscribe 식별자", required = true) Long subscribeId,
            @RequestBody SubscribePostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "특정 회원의 구독중 상품 상태 수정(관리자)", description = "구독중 상품 수정 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            ,schema = @Schema(implementation = SubscribeUserResponse.class)
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> patchSubscribeUser(
            @PathVariable @Parameter(description = "회원의 현재 Subscribe Data 식별자", required = true) Long subscribeUserId,
            @RequestBody SubscribeUserPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "구독 상품 삭제(관리자)", description = "구독 상품 삭제 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                    )}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> deleteSubscribe(
            @PathVariable @Parameter(description = "구독 상품 식별자", required = true) Long subscribeId,
            @UserSession @Parameter(hidden = true) User user
    );

}

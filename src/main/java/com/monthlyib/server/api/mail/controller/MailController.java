package com.monthlyib.server.api.mail.controller;


import com.monthlyib.server.api.mail.dto.MailPostRequestDto;
import com.monthlyib.server.api.monthlyib.controller.MonthlyIbApiControllerIfs;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "K. Mail", description = "메일 관리 API")
public class MailController {

    private final MailService mailService;

    @Operation(summary = "메일 전송(관리자)", description = "메일 전송 요청(관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답",
                    content = {@Content(mediaType = "application/json"
                            , schema = @Schema(implementation = MonthlyIbApiControllerIfs.PageResponse.class)

                    )
                    }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("/api/mail")
    ResponseEntity<ResponseDto<?>> postMail(
            @RequestBody MailPostRequestDto requestDto
    ) {
        boolean b = mailService.sendMail(requestDto);
        if (b) {
            return ResponseEntity.ok(ResponseDto.of(Result.ok()));

        } else {
            return ResponseEntity.ok(ResponseDto.of(Result.error()));
        }
    }
}

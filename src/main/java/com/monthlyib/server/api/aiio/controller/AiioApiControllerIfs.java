package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.api.aiio.dto.AiioResponseDto;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.annotation.UserSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "E. Aiio", description = "AI IO 피드백 관련 API")
public interface AiioApiControllerIfs {

    class AiioResponse extends ResponseDto<AiioResponseDto> { }

    @Operation(summary = "AI IO 피드백 생성", description = "대본 파일과 녹음 파일을 받아서 AI 피드백을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "정상 응답",
                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AiioResponse.class))}),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ResponseDto<?>> postAiioFeedback(
            @ModelAttribute AiioPostDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );

    @Operation(summary = "AI IO 피드백 수정", description = "기존 AI IO 피드백을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "정상 응답",
                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AiioResponse.class))}),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ResponseDto<?>> patchAiioFeedback(
            @ModelAttribute AiioPatchDto requestDto,
            @UserSession @Parameter(hidden = true) User user
    );
}
package com.monthlyib.server.openapi.user.controller;


import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.openapi.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "A. OPEN API", description = "OPEN API(권한 없이 요청 가능)")
public interface UserOpenApiControllerIfs {

    class LoginResponse extends ResponseDto<LoginApiResponseDto> {}

    @Operation(summary = "로그인 요청", description = "바디 데이터 응답 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ACCESS DENIED(아이디 혹은 비밀번호 틀림)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> login(
            @RequestBody LoginDto loginDto
    );

    @Operation(summary = "소셜 로그인 / 회원가입 요청", description = "바디 데이터 응답 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ACCESS DENIED(아이디 혹은 비밀번호 틀림)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> loginSocial(
            @RequestBody SocialLoginDto loginDto,
            HttpServletResponse response
    );

    @Operation(summary = "네이버 소셜 로그인 / 회원가입 요청", description = "바디 데이터 응답 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "ACCESS DENIED(아이디 혹은 비밀번호 틀림)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> loginSocialNaver(
            @RequestBody NaverLoginRequest loginDto,
            HttpServletResponse response
    );

    @Operation(summary = "토큰 재발급", description = "바디 데이터 응답 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> refreshToken(
            @PathVariable @Parameter(description = "회원의 식별자", required = true) Long userId
    );

    class UserResponse extends ResponseDto<UserResponseDto> {}

    @Operation(summary = "회원 가입 요청", description = "200 OK 응답 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> register(
            @RequestBody UserPostRequestDto requestDto
    );

    // 회원 가입 이메일 인증 번호 발송 요청
    @Operation(summary = "메일 인증번호 요청", description = "200 OK 응답 확인, 회원 가입시 메일 인증번호 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> verifyEmailPost(
            @RequestBody EmailRequestDto requestDto
    );

    // 비밀번호 분실 이메일 인증 번호 발송 요청
    @Operation(summary = "메일 인증번호 요청", description = "200 OK 응답 확인, 비밀번호 분실 메일 인증번호 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> verifyPwdEmailPost(
            @RequestBody EmailRequestDto requestDto
    );

    // 이메일 인증 번호 확인 여부 요청
    @Operation(summary = "인증번호 검증 요청", description = "200 OK 응답 확인, 인증번호 검증 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> verifyNumPost(
            @RequestBody VerifyNumRequestDto requestDto
    );

    // 이메일 인증 번호 확인 여부 요청
    @Operation(summary = "회원 아이디 검증 요청", description = "200 OK 응답 확인, 회원 아이디 검증 요청")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    ResponseEntity<ResponseDto<?>> verifyUsernamePost(
            @RequestBody UsernameVerifyDto requestDto
    );


}

package com.monthlyib.server.openapi.user.controller;


import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.openapi.user.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-api")
@RequiredArgsConstructor
@Slf4j
public class UserOpenApiController implements UserOpenApiControllerIfs {

    private final UserService userService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<?>> login(
            LoginDto loginDto
    ) {
        log.info("# Verify Login User");
        LoginApiResponseDto response = userService.userLogin(loginDto);
        return ResponseEntity.ok().body(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/login/social")
    public ResponseEntity<ResponseDto<?>> loginSocial(SocialLoginDto loginDto, HttpServletResponse servletResponse) {
        log.info("# Verify Social Login User");
        LoginApiResponseDto response = userService.loginSocial(loginDto, servletResponse);
        return ResponseEntity.ok().body(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/login/naver")
    public ResponseEntity<ResponseDto<?>> loginSocialNaver(NaverLoginRequest loginDto, HttpServletResponse servletResponse) {
        log.info("# Verify Social NAVER Login User");
        LoginApiResponseDto response = userService.loginSocialNaver(loginDto, servletResponse);
        return ResponseEntity.ok().body(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/reissue-token/{userId}")
    public ResponseEntity<ResponseDto<?>> refreshToken(Long userId) {
        LoginApiResponseDto response = userService.refreshToken(userId);
        log.info("# Reissue Token");
        return ResponseEntity.ok().body(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<ResponseDto<?>> register(
            UserPostRequestDto requestDto
    ) {
        UserResponseDto response = userService.createUser(requestDto);
        return ResponseEntity.ok().body(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<ResponseDto<?>> verifyEmailPost(EmailRequestDto requestDto) {
        userService.verifyEmailNumPost(requestDto);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/pwd-email")
    public ResponseEntity<ResponseDto<?>> verifyPwdEmailPost(EmailRequestDto requestDto) {
        userService.verifyPwdEmail(requestDto);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/verify-num")
    public ResponseEntity<ResponseDto<UserResponseDto>> verifyNumPost(VerifyNumRequestDto requestDto) {
        UserResponseDto response = userService.verifyNum(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response,Result.ok()));
    }

    @Override
    @PostMapping("/verify-username")
    public ResponseEntity<ResponseDto<?>> verifyUsernamePost(UsernameVerifyDto requestDto) {
        userService.verifyUsername(requestDto);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

}

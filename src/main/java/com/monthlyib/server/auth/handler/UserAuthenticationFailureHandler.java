package com.monthlyib.server.auth.handler;

import com.monthlyib.server.constant.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class UserAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /* 인증에 실패 했을 경우 핸들링 */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        log.error("# Authentication failed: {}", exception.getMessage());
        if (exception.getMessage().equals(ErrorCode.USER_INACTIVE.getMessage())) {
            ErrorResponder.sendErrorResponse(response, ErrorCode.USER_INACTIVE);
        } else {
            ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
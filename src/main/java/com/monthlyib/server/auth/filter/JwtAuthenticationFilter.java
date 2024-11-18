package com.monthlyib.server.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.openapi.user.dto.LoginDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;


    /* Login Dto로 전송받은 데이터를
     * UsernamePasswordAuthenticationToken 으로 변환 하여 Security Flow 시작 */
    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        log.info("#### JwtAuthenticationFilter attemptAuthentication()");
        String header = request.getHeader("Request-type");
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = null;
        if (header == null) {
            usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            request.getParameter("username"),
                            request.getParameter("password")
                    );
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
            usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        }

        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    }

    /* 인증에 성공시 응답 헤더 , 쿠키 설정 */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {
        log.info("### JwtAuthenticationFilter successfulAuthentication()");

        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

}
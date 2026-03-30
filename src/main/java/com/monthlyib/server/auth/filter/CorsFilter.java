package com.monthlyib.server.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "https://www.monthly-ib.com",
            "https://monthly-ib.com"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String originUrl = request.getHeader("Origin");
        boolean allowedOrigin = originUrl != null && ALLOWED_ORIGINS.contains(originUrl);
        if (allowedOrigin) {
            response.setHeader("Access-Control-Allow-Origin", originUrl);
            response.setHeader("Vary", "Origin");
        }
        response.setHeader("Access-Control-Allow-Methods","GET, POST, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, userId, userStatus, Content-Disposition");
        if (allowedOrigin) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Key, Authorization, Authorization, userId, userStatus, Content-Disposition, Timeout");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(allowedOrigin ? HttpServletResponse.SC_OK : HttpServletResponse.SC_FORBIDDEN);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

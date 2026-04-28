package com.monthlyib.server.filter;

import com.monthlyib.server.utils.WebUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class LoggerFilter extends OncePerRequestFilter {


    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!shouldLog(uri)) {
            chain.doFilter(request, response);
            return;
        }

        long startNanos = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info(
                    "[Access] method={} uri={} status={} elapsedMs={} ip={} device={}",
                    request.getMethod(),
                    uri,
                    response.getStatus(),
                    elapsedMs,
                    WebUtils.ip(),
                    WebUtils.device()
            );
        }
    }

    private boolean shouldLog(String uri) {
        return !(uri.contains("/api/sse")
                || uri.contains("/actuator")
                || uri.equals("/")
                || uri.contains("/docs")
                || uri.contains("/api-docs")
                || uri.contains("/swagger")
                || uri.contains("/vendor")
                || uri.contains("/alarm")
                || uri.contains("css")
                || uri.contains("js")
                || uri.contains("images")
                || uri.contains("favicon")
                || uri.contains("icon"));
    }
}

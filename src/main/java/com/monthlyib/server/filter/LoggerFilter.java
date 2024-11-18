package com.monthlyib.server.filter;

import com.monthlyib.server.utils.WebUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

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
        String requestURI = request.getRequestURI();
        if (!requestURI.contains("/api/sse")) {
            ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
            ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);

            chain.doFilter(req, res);

            // Request 정보
            StringBuilder requestHeaders = new StringBuilder();
            req.getHeaderNames().asIterator().forEachRemaining(
                    key -> {
                        String value = req.getHeader(key);
                        requestHeaders.append("[").append(key).append(" : ").append(value).append("] ").append(" , ");

                    }
            );
            String requestBody = new String(req.getContentAsByteArray());
            if (requestBody.length() > 200) {
                requestBody = requestBody.substring(0, 200);
            }
            String URI = req.getRequestURI();
            String method = req.getMethod();
            boolean checkedUri = !(URI.contains("/actuator") || URI.equals("/")
                    || URI.contains("/docs")
                    || URI.contains("/vendor")
                    || URI.contains("/alarm")
                    || URI.contains("css")
                    || URI.contains("js")
                    || URI.contains("images")
            );
            if (checkedUri) {
                log.info(">>>>>> [Request] URI : {} , Method : {} , Header : {} ,  body : {}, IP : {}, Device : {}", URI, method, requestHeaders, requestBody, WebUtils.ip(), WebUtils.device());
            }

            StringBuilder responseHeaders = new StringBuilder();
            res.getHeaderNames().forEach(
                    key -> {
                        String value = res.getHeader(key);
                        responseHeaders.append("[").append(key).append(" : ").append(value).append("] ").append(" , ");
                    }
            );
            String responseBody = new String(res.getContentAsByteArray());
            if (responseBody.length() > 200) {
                responseBody = responseBody.substring(0, 200);
            }
            if (checkedUri) {
                log.info("<<<<<< [Response] URI : {} , Method : {} Header : {} ,  body : {}", URI, method, responseHeaders, responseBody);
            }

            // Response 를 스위칭 했기에 다시 카피 해준다
            res.copyBodyToResponse();
        } else {
            chain.doFilter(request, response);
        }
        //ServletRequest 는 한번 스위칭 하면 후에 사용할수 없음 그래서 Wrapper로 감싸준다


    }

}
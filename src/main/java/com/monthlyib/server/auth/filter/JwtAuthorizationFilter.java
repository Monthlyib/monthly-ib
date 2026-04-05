package com.monthlyib.server.auth.filter;


import com.monthlyib.server.auth.jwt.JwtTokenizer;
import com.monthlyib.server.auth.util.CustomAuthorityUtils;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenizer jwtTokenizer;

    private final CustomAuthorityUtils authorityUtils;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            log.debug("### Access Process [API Request]");
            Map<String, Object> claims = getHeaderClaims(request);
            User user = verifyCurrentSession(claims);
            setAuthenticationToContext(claims, user);
            RequestAttributes requestContext = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
            requestContext.setAttribute("userId", claims.get("userId"), RequestAttributes.SCOPE_REQUEST);
        } catch (ExpiredJwtException ee) {
            request.setAttribute(
                    "exception",
                    new ServiceLogicException(ErrorCode.EXPIRED_ACCESS_TOKEN));
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) throws ServletException {
        String authorization = request.getHeader("Authorization");

        return authorization == null || !authorization.startsWith("Bearer ");
    }

    private Map<String, Object> getHeaderClaims(HttpServletRequest request) {
        String jwt = "";
        String findHeader = request.getHeader("Authorization").replace("Bearer ", "");
        if (findHeader == null || findHeader.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED_REQUEST_API);
        } else {
            jwt = findHeader.replace("Bearer ", "");
        }
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        log.debug("### Success Find AccessToken In Header [API Request]");
        return jwtTokenizer.getClaims(jwt, base64EncodedSecretKey).getBody();
    }

    private void setAuthenticationToContext(Map<String, Object> claims, User user) {
        String username = (String) claims.get("username");
        List<String> rolesList = user.getAuthority() == Authority.ADMIN
                ? Authority.ADMIN.getStringRole()
                : Authority.USER.getStringRole();
        List<GrantedAuthority> roles = authorityUtils.createAuthorities(rolesList);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(username, null, roles);
        log.debug("### SecurityContextHolder SetAuthentication = {}", rolesList);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User verifyCurrentSession(Map<String, Object> claims) {
        Object userId = claims.get("userId");
        if (userId == null) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED_REQUEST_API);
        }

        User user = userRepository.findById(Long.parseLong(userId.toString()))
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

        long tokenSessionVersion = Long.parseLong(String.valueOf(claims.getOrDefault("sessionVersion", 0L)));
        long currentSessionVersion = user.getSessionVersion() == null ? 0L : user.getSessionVersion();

        if (tokenSessionVersion != currentSessionVersion) {
            throw new ServiceLogicException(ErrorCode.SESSION_EXPIRED_BY_NEW_LOGIN);
        }

        LocalDateTime refreshThreshold = LocalDateTime.now().minusMinutes(5);
        if (user.getLastAccessAt() == null || user.getLastAccessAt().isBefore(refreshThreshold)) {
            user.touchLastAccessAt();
            userRepository.save(user);
        }

        return user;
    }
}

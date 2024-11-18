package com.monthlyib.server.auth.jwt;


import com.monthlyib.server.auth.service.RefreshService;
import com.monthlyib.server.auth.token.Token;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenizer {

    @Setter
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    private final int accessTokenExpirationMinutes = 10;

    private final int refreshTokenExpirationMinutes = 50000;

    private final RefreshService refreshService;



    public String encodeBase64SecretKey(String secretKey) {
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /* jwt 토큰을 생성 */
    private Token generateToken(
            Map<String, Object> claims,
            String subject,
            String base64EncodedSecretKey
    ) {
        Key key = getKeyFromBase64EncodedSecretKey(base64EncodedSecretKey);

        return new Token(
                "Bearer " + Jwts.builder()
                        .setClaims(claims)
                        .setSubject(subject)
                        .setIssuedAt(Calendar.getInstance().getTime())
                        .setExpiration(getTokenExpiration(accessTokenExpirationMinutes))
                        .signWith(key)
                        .compact());

    }

    private Token generateRefreshToken(
            Map<String, Object> claims,
            String subject,
            String base64EncodedSecretKey
    ) {
        Key key = getKeyFromBase64EncodedSecretKey(base64EncodedSecretKey);

        return new Token(
                "Bearer " + Jwts.builder()
                        .setClaims(claims)
                        .setSubject(subject)
                        .setIssuedAt(Calendar.getInstance().getTime())
                        .setExpiration(getTokenExpiration(refreshTokenExpirationMinutes))
                        .signWith(key)
                        .compact());

    }

    /* user 매개변수를 받아 jwt 토큰을 생성 */

    public Token delegateToken(
            User user
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles());
        String subject = user.getUsername();
        String base64SecretKey = encodeBase64SecretKey(getSecretKey());
        Token refreshToken = generateRefreshToken(claims, subject, base64SecretKey);
        refreshService.createRefresh(user.getUsername(), refreshToken.getAccessToken());
        return generateToken(claims, subject, base64SecretKey);
    }


    /* AccessToken 검증 */
    public void verifyAccessToken(
            String accessToken
    ) {
        String base64SecretKey = encodeBase64SecretKey(getSecretKey());
        try {
            verifySignature(getJws(accessToken), base64SecretKey);
        } catch (ExpiredJwtException ee) {
            throw new ServiceLogicException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (Exception e) {
            throw e;
        }
    }

    /* Server에서 발급한 토큰이 맞는지 검증 */
    private void verifySignature(String jwt, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedSecretKey(base64EncodedSecretKey);

        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt);
    }

    /* Claims 정보를 가져옴 */
    public Jws<Claims> getClaims(String jwt, String base64EncodedSecretKey) {
        verifyAccessToken(jwt);
        Key key = getKeyFromBase64EncodedSecretKey(base64EncodedSecretKey);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt);
    }

    /* Token의 만료 기한 설정 */
    private Date getTokenExpiration(int expirationMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        return calendar.getTime();
    }

    /* Token에서 UserId 정보를 가져옴 */
    public String getUsername(String token) {
        Key key = getKeyFromBase64EncodedSecretKey(encodeBase64SecretKey(secretKey));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(getJws(token))
                .getBody()
                .getSubject()
                ;
    }


    /* Secret key 생성 */
    private Key getKeyFromBase64EncodedSecretKey(String base64EncodedSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getJws(String accessToken) {
        return accessToken.replace("Bearer ", "");
    }

}
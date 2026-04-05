package com.monthlyib.server.auth.service;

import com.monthlyib.server.auth.dto.GoogleTokenResponse;
import com.monthlyib.server.auth.dto.GoogleUserInfo;
import com.monthlyib.server.auth.dto.OauthInfo;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.openapi.user.dto.GoogleLoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri:}")
    private String configuredRedirectUri;

    @Value("${oauth.google.url.auth}")
    private String authUrl;

    @Value("${oauth.google.url.api}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public OauthInfo getGoogleInfo(GoogleLoginRequest googleLoginRequest) {
        validateGoogleLoginRequest(googleLoginRequest);

        String redirectUri = resolveRedirectUri(googleLoginRequest.getRedirectUri());
        GoogleTokenResponse tokenResponse = getOauthAccessToken(
                googleLoginRequest.getAuthorizationCode(),
                redirectUri
        );
        GoogleUserInfo profile = getOauthProfile(tokenResponse.getAccessToken());

        if (profile == null || profile.getEmail() == null || profile.getEmail().isBlank()) {
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google 계정 이메일 정보를 확인할 수 없습니다."
            );
        }

        if (Boolean.FALSE.equals(profile.getEmailVerified())) {
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "이메일 인증이 완료된 Google 계정만 사용할 수 있습니다."
            );
        }

        return OauthInfo.builder()
                .email(profile.getEmail())
                .nickname(profile.getName())
                .loginType(LoginType.GOOGLE)
                .authority(Authority.USER)
                .build();
    }

    private GoogleTokenResponse getOauthAccessToken(String authorizationCode, String redirectUri) {
        String url = authUrl + "/token";

        HttpHeaders headers = newHttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response =
                    restTemplate.postForEntity(url, request, GoogleTokenResponse.class);
            GoogleTokenResponse responseBody = response.getBody();
            if (responseBody == null || responseBody.getAccessToken() == null || responseBody.getAccessToken().isBlank()) {
                throw new ServiceLogicException(
                        ErrorCode.BAD_REQUEST,
                        "Google access token 교환에 실패했습니다."
                );
            }

            return responseBody;
        } catch (RestClientException e) {
            log.warn("Google token exchange failed: {}", e.getMessage());
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google 로그인 인증 정보가 유효하지 않습니다."
            );
        }
    }

    private GoogleUserInfo getOauthProfile(String accessToken) {
        String url = apiUrl + "/v1/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> response =
                    restTemplate.exchange(url, HttpMethod.GET, request, GoogleUserInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("Google userinfo request failed: {}", e.getMessage());
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google 사용자 정보를 가져오지 못했습니다."
            );
        }
    }

    private String resolveRedirectUri(String requestRedirectUri) {
        if (configuredRedirectUri != null && !configuredRedirectUri.isBlank()) {
            if (requestRedirectUri != null
                    && !requestRedirectUri.isBlank()
                    && !configuredRedirectUri.equals(requestRedirectUri)) {
                throw new ServiceLogicException(
                        ErrorCode.BAD_REQUEST,
                        "허용되지 않은 Google redirectUri 입니다."
                );
            }
            return configuredRedirectUri;
        }

        if (requestRedirectUri == null || requestRedirectUri.isBlank()) {
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google redirectUri가 필요합니다."
            );
        }

        return requestRedirectUri;
    }

    private void validateGoogleLoginRequest(GoogleLoginRequest requestDto) {
        if (requestDto == null || requestDto.getAuthorizationCode() == null || requestDto.getAuthorizationCode().isBlank()) {
            throw new ServiceLogicException(
                    ErrorCode.BAD_REQUEST,
                    "Google authorizationCode가 필요합니다."
            );
        }

        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ServiceLogicException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Google OAuth 설정이 누락되었습니다."
            );
        }
    }

    private HttpHeaders newHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return httpHeaders;
    }
}

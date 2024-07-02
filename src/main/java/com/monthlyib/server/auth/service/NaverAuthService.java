package com.monthlyib.server.auth.service;

import com.monthlyib.server.auth.dto.NaverMyInfo;
import com.monthlyib.server.auth.dto.NaverToken;
import com.monthlyib.server.auth.dto.OauthInfo;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.openapi.user.dto.NaverLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NaverAuthService {
    @Value("${oauth.naver.url.auth}")
    private String authUrl; //https://nid.naver.com

    @Value("${oauth.naver.url.api}")
    private String apiUrl;

    @Value("${oauth.naver.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;


    public String getOauthAccessToken(String grantType, String clientId, String code, String state) {
        String url = authUrl + "/oauth2.0/token";

        HttpHeaders httpHeaders = newHttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("state", state);
        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<NaverToken> response = restTemplate.postForEntity(url, request, NaverToken.class);

        return Objects.requireNonNull(response.getBody()).getAccessToken();
    }


    public OauthInfo getNaverInfo(NaverLoginRequest naverLoginRequest) {
        String accessToken = getOauthAccessToken(
                naverLoginRequest.getGrantType(),
                naverLoginRequest.getClientId(),
                naverLoginRequest.getAuthorizationCode(),
                naverLoginRequest.getState()
        );
        NaverMyInfo oauthProfile = getOauthProfile(accessToken);

        return OauthInfo.builder()
                .email(oauthProfile.getEmail())
                .nickname(oauthProfile.getNickName())
                .loginType(LoginType.NAVER)
                .authority(Authority.USER)
                .build();
    }

    public NaverMyInfo getOauthProfile(String accessToken) {
        String url = apiUrl + "/v1/nid/me";

        HttpHeaders httpHeaders = newHttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> request = new HttpEntity<>(httpHeaders);
        ResponseEntity<NaverMyInfo> response = restTemplate.postForEntity(url, request, NaverMyInfo.class);

        return response.getBody();
    }

    public LoginType getLoginType() {
        return LoginType.NAVER;
    }

    private HttpHeaders newHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return httpHeaders;
    }

}

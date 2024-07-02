package com.monthlyib.server.domain.user.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.monthlyib.server.constant.BaseUrl;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.openapi.user.dto.SocialLoginDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpClientService {


    public String generateLoginRequest(SocialLoginDto dto) {
        String loginType = dto.getLoginType();
        String requestUrl;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            if (loginType.equals("KAKAO")) {
                log.info("Kakao Login Request");
                log.info("Kakao Login Token = {}", dto.getOauthAccessToken());
                requestUrl = createKakaoLoginRequestUrl();
                HttpGet httpGet = new HttpGet(requestUrl);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                httpGet.setHeader(HttpHeaders.AUTHORIZATION,"Bearer "+dto.getOauthAccessToken());
                return (String) httpclient.execute(httpGet, getLoginHandler(loginType));
            } else if (loginType.equals("NAVER")) {
                log.info("Naver Login Request");
                log.info("Naver Login Token = {}", dto.getOauthAccessToken());
                requestUrl = createNaverLoginRequestUrl();
                HttpGet httpGet = new HttpGet(requestUrl);
                httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                httpGet.setHeader("Authorization","Bearer "+dto.getOauthAccessToken());
                return (String) httpclient.execute(httpGet, getLoginHandler(loginType));
            } else {
                log.info("Google Login Request");
                log.info("Google Login Token = {}", dto.getOauthAccessToken());
                requestUrl = createGoogleLoginRequestUrl(dto.getOauthAccessToken());
                HttpGet httpGet = new HttpGet(requestUrl);
                return (String) httpclient.execute(httpGet, getLoginHandler(loginType));
            }
        } catch (IOException e) {
            throw new ServiceLogicException(ErrorCode.HTTP_REQUEST_IO_ERROR);
        }
    }
    private ResponseHandler<?> getLoginHandler(String loginType) {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity responseBody = response.getEntity();
                String res = EntityUtils.toString(responseBody);
                if (loginType.equals("KAKAO")) {
                    JsonElement kakaoElement = JsonParser.parseString(res);
                    JsonElement kakaoAccount = kakaoElement.getAsJsonObject().get("kakao_account");
                    String email = kakaoAccount.getAsJsonObject().get("email").getAsString();
                    return email;
                } else if (loginType.equals("NAVER")) {
                    JsonElement naverElement = JsonParser.parseString(res);
                    JsonElement naverAccount = naverElement.getAsJsonObject().get("response");
                    String email = naverAccount.getAsJsonObject().get("email").getAsString();
                    return email;
                } else {
                    JsonElement googleElement = JsonParser.parseString(res);
                    String email = googleElement.getAsJsonObject().get("email").getAsString();
                    log.info("Google Email Account = {}", email);
                    return email;
                }

            } else {
                //Todo : Status Code 활용하여 예외처리
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }


    public static String createGoogleLoginRequestUrl(String token) {
        StringBuffer sb = new StringBuffer();
        sb.append(BaseUrl.GOOGLE_LOGIN.getUrl());
        sb.append(token);
        return sb.toString();
    }

    public static String createKakaoLoginRequestUrl() {
        return BaseUrl.KAKAO_LOGIN.getUrl();
    }

    public static String createNaverLoginRequestUrl() {
        return BaseUrl.NAVER_LOGIN.getUrl();
    }
}

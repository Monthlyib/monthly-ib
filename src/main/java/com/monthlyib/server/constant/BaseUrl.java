package com.monthlyib.server.constant;

import lombok.Getter;

public enum BaseUrl {
    GOOGLE_LOGIN("https://www.googleapis.com/oauth2/v1/userinfo?access_token="),
    KAKAO_LOGIN("https://kapi.kakao.com/v2/user/me"),
    NAVER_LOGIN("https://openapi.naver.com/v1/nid/me");


    @Getter
    final String url;

    BaseUrl(String url) {
        this.url = url;
    }

}

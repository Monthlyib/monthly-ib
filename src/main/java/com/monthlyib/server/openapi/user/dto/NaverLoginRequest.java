package com.monthlyib.server.openapi.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverLoginRequest {

    private String grantType;

    private String clientId;

    private String authorizationCode;

    private String state;

}

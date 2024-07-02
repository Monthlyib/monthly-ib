package com.monthlyib.server.auth.dto;

import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.LoginType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OauthInfo {
    private final String nickname;
    private final String email;
    private final LoginType loginType;
    private final Authority authority;
}
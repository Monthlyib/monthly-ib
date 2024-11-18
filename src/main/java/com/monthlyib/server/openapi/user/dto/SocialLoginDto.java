package com.monthlyib.server.openapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SocialLoginDto {

    String oauthAccessToken;

    String loginType;

}
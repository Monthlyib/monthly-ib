package com.monthlyib.server.openapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "로그인 응답 DTO")
public class LoginResponseDto {

    @Schema(description = "회원 식별자")
    private String userId;

    @Schema(description = "AccessToken")
    private String accessToken;

    @Schema(description = "회원 권한")
    private String userAuth;


    public static LoginResponseDto of(String accessToken, Long userId, String userAuth) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .userId(String.valueOf(userId))
                .userAuth(userAuth)
                .build();
    }


}
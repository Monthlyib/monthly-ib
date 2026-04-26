package com.monthlyib.server.openapi.user.dto;

import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.auth.token.Token;
import com.monthlyib.server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "로그인 응답 DTO")
public class LoginApiResponseDto {

    @Schema(description = "AccessToken")
    private String accessToken;

    @Schema(description = "RefreshToken")
    private String refreshToken;

    @Schema(description = "회원 식별자")
    private Long userId;

    @Schema(description = "회원 아이디/이메일")
    private String username;

    @Schema(description = "회원 닉네임")
    private String nickname;

    @Schema(description = "회원 닉네임")
    private String email;

    @Schema(description = "회원 권한")
    private Authority authority;

    @Schema(description = "회원 상태")
    private UserStatus userStatus;

    @Schema(description = "연결된 로그인 수단")
    private List<LoginType> linkedProviders;


    public static LoginApiResponseDto of(Token token, User user, List<LoginType> linkedProviders) {
        return LoginApiResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickName())
                .email(user.getEmail())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .authority(user.getAuthority())
                .userStatus(user.getUserStatus())
                .linkedProviders(linkedProviders)
                .build();
    }

}

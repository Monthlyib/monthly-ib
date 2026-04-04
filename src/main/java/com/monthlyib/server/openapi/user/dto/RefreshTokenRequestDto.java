package com.monthlyib.server.openapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "리프레시 토큰 재발급 요청 DTO")
public class RefreshTokenRequestDto {

    @Schema(description = "RefreshToken", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}

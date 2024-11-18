package com.monthlyib.server.openapi.user.dto;

import com.monthlyib.server.auth.entity.RefreshEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshDto {

    private String username;

    private String refreshToken;

    private LocalDateTime createdAt;

    public RefreshDto(RefreshEntity entity) {
        this.username = entity.getUsername();
        this.refreshToken = entity.getRefreshToken();
        this.createdAt = entity.getCreatedAt();
    }

    public static RefreshDto of(RefreshEntity entity) {
        return new RefreshDto(entity);
    }
}
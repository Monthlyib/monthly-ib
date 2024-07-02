package com.monthlyib.server.auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
// 1달 2629800 sec
@RedisHash(value = "Refresh", timeToLive = 2629800)
public class RefreshEntity {

    @Id
    private String username;

    private String refreshToken;

    private LocalDateTime createdAt;

    public RefreshEntity(String username, String refreshToken) {
        this.username = username;
        this.refreshToken = refreshToken;
        this.createdAt = LocalDateTime.now();
    }
}

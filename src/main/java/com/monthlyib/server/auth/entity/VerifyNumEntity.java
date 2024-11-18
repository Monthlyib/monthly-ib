package com.monthlyib.server.auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Getter
@Setter
// 1달 2629800 sec
@RedisHash(value = "VerifyNum", timeToLive = 1800)
public class VerifyNumEntity {

    @Id
    private String email;

    private String verifyNum;

    private LocalDateTime createdAt;

    public VerifyNumEntity(String email, String verifyNum) {
        this.email = email;
        this.verifyNum = verifyNum;
        this.createdAt = LocalDateTime.now();
    }
}

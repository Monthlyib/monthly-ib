package com.monthlyib.server.constant;

import lombok.Getter;

@Getter
public enum UserStatus {

    ACTIVE("활성화"),
    WAIT_INFO("회원 정보 미입력"),
    VERIFY_EMAIL_WAIT("이메일 인증 대기"),
    INACTIVE("탈퇴");

    private final String status;

    UserStatus(String status) {
        this.status = status;
    }

}

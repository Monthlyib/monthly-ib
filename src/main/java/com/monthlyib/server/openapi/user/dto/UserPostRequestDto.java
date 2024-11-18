package com.monthlyib.server.openapi.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "회원 생성 DTO")
public class UserPostRequestDto {

    private String username;

    private String password;

    private String nickName;

    private String email;

    private String birth;

    private String school;

    private String grade;

    private String address;

    private String country;

    private String verifyNum;

    private boolean termsOfUseCheck;

    private boolean privacyTermsCheck;

    private boolean marketingTermsCheck;

}

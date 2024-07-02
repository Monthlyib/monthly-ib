package com.monthlyib.server.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSocialPatchRequestDto {

    private String username;

    private String nickName;

    private String birth;

    private String school;

    private String grade;

    private String address;

    private String country;

    private boolean termsOfUseCheck;

    private boolean privacyTermsCheck;

    private boolean marketingTermsCheck;

}

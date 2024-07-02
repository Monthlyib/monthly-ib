package com.monthlyib.server.api.user.dto;

import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPatchRequestDto {

    private String password;

    private String nickName;

    private String email;

    private String birth;

    private String school;

    private String grade;

    private String address;

    private String country;

    private UserStatus userStatus;

    private Authority authority;

    private String memo;

    private boolean marketingTermsCheck;


}

package com.monthlyib.server.api.user.dto;

import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.entity.UserImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;

    private String username;

    private String nickName;

    private String email;

    private String birth;

    private String school;

    private String grade;

    private String address;

    private String memo;

    private String country;

    private boolean termsOfUseCheck;

    private boolean privacyTermsCheck;

    private boolean marketingTermsCheck;

    private UserStatus userStatus;

    private LoginType loginType;

    private List<LoginType> linkedProviders;

    private Authority authority;

    private UserImageResponseDto userImage;


    public static UserResponseDto of(User user, UserImage userImage) {
        return of(user, userImage, List.of(user.getLoginType()));
    }

    public static UserResponseDto of(User user, UserImage userImage, List<LoginType> linkedProviders) {
        UserResponseDtoBuilder userBuilder = UserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .birth(user.getBirth())
                .school(user.getSchool())
                .grade(user.getGrade())
                .address(user.getAddress())
                .memo(user.getMemo())
                .country(user.getCountry())
                .termsOfUseCheck(user.isTermsOfUseCheck())
                .privacyTermsCheck(user.isPrivacyTermsCheck())
                .marketingTermsCheck(user.isMarketingTermsCheck())
                .userStatus(user.getUserStatus())
                .loginType(user.getLoginType())
                .linkedProviders(linkedProviders)
                .authority(user.getAuthority());
        if (userImage != null) {
            userBuilder.userImage(UserImageResponseDto.of(userImage));
        } else {
            userBuilder.userImage(null);
        }

        return userBuilder.build();
    }

}

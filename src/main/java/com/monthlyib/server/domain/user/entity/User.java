package com.monthlyib.server.domain.user.entity;


import com.monthlyib.server.api.user.dto.UserPatchRequestDto;
import com.monthlyib.server.api.user.dto.UserSocialPatchRequestDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.LoginType;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.openapi.user.dto.UserPostRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false, unique = true)
    private String email;

    private Long mergedIntoUserId;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String school;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String memo;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private boolean termsOfUseCheck;

    @Column(nullable = false)
    private boolean privacyTermsCheck;

    @Column(nullable = false)
    private boolean marketingTermsCheck;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Builder.Default
    @Column(nullable = false)
    private Long sessionVersion = 0L;

    private LocalDateTime lastAccessAt;


    /*
    * 사용하지 않음
    * */

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private BigDecimal subscriptionPrice;

    @Column(nullable = false)
    private int remainQuestionCount;

    // 30일 기준 수업 시간
    @Column(nullable = false)
    private int remainTutoringCount;
    // 강의 수강 가능 과목
    @ElementCollection(fetch = FetchType.LAZY)
    private List<Long> videoLessonsIdList = new ArrayList<>();
    // 만료 기한
    @Column(nullable = true)
    private LocalDate expirationDate;

    // 구독 일자
    @Column(nullable = true)
    private LocalDate subscriptionDate;

    @Column(nullable = false)
    private int subscriptionDay;

    // ----------------------

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    public void syncRolesToAuthority() {
        this.roles = this.authority == Authority.ADMIN
                ? new ArrayList<>(Authority.ADMIN.getStringRole())
                : new ArrayList<>(Authority.USER.getStringRole());
    }

    public static User createEmptyUser(String email, String loginType, String username, String nickName) {
        User user = User.builder()
                .username(username)
                .nickName(nickName)
                .password(java.util.UUID.randomUUID().toString())
                .email(email)
                .birth("0")
                .school("정보 없음")
                .grade("정보 없음")
                .address("정보 없음")
                .memo("정보 없음")
                .country("정보 없음")
                .userStatus(UserStatus.WAIT_INFO)
                .loginType(LoginType.valueOf(loginType))
                .sessionVersion(0L)
                .authority(Authority.USER)
                .roles(new ArrayList<>(Authority.USER.getStringRole()))
                .videoLessonsIdList(new ArrayList<>())
                .subscriptionId(0L)
                .subscriptionPrice(BigDecimal.ZERO)
                .remainTutoringCount(0)
                .remainQuestionCount(0)
                .build();
        user.syncRolesToAuthority();
        return user;
    }

    public static User createUser(UserPostRequestDto dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .nickName(dto.getNickName())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .birth(dto.getBirth())
                .school(dto.getSchool())
                .grade(dto.getGrade())
                .address(dto.getAddress())
                .country(dto.getCountry())
                .memo("")
                .userStatus(UserStatus.ACTIVE)
                .loginType(LoginType.BASIC)
                .sessionVersion(0L)
                .roles(new ArrayList<>(Authority.USER.getStringRole()))
                .videoLessonsIdList(new ArrayList<>())
                .termsOfUseCheck(dto.isTermsOfUseCheck())
                .privacyTermsCheck(dto.isPrivacyTermsCheck())
                .marketingTermsCheck(dto.isMarketingTermsCheck())
                .authority(Authority.USER)
                .subscriptionId(0L)
                .subscriptionPrice(BigDecimal.ZERO)
                .remainTutoringCount(0)
                .remainQuestionCount(0)
                .build();
        user.syncRolesToAuthority();
        return user;
    }

    public User updateUser(UserPatchRequestDto dto) {
        this.password = Optional.ofNullable(dto.getPassword()).orElse(this.password);
        this.nickName = Optional.ofNullable(dto.getNickName()).orElse(this.nickName);
        this.email = Optional.ofNullable(dto.getEmail()).orElse(this.email);
        this.birth = Optional.ofNullable(dto.getBirth()).orElse(this.birth);
        this.school = Optional.ofNullable(dto.getSchool()).orElse(this.school);
        this.grade = Optional.ofNullable(dto.getGrade()).orElse(this.grade);
        this.address = Optional.ofNullable(dto.getAddress()).orElse(this.address);
        this.country = Optional.ofNullable(dto.getCountry()).orElse(this.country);
        this.userStatus = Optional.ofNullable(dto.getUserStatus()).orElse(this.userStatus);
        this.authority = Optional.ofNullable(dto.getAuthority()).orElse(this.authority);
        this.marketingTermsCheck = Optional.ofNullable(dto.isMarketingTermsCheck()).orElse(this.marketingTermsCheck);
        this.memo  = Optional.ofNullable(dto.getMemo()).orElse(this.memo);
        syncRolesToAuthority();
        return this;
    }


    public User updateSocialUser(UserSocialPatchRequestDto dto) {
        this.username = Optional.ofNullable(dto.getUsername()).orElse(this.username);
        this.nickName = Optional.ofNullable(dto.getNickName()).orElse(this.nickName);
        this.birth = Optional.ofNullable(dto.getBirth()).orElse(this.birth);
        this.school = Optional.ofNullable(dto.getSchool()).orElse(this.school);
        this.grade = Optional.ofNullable(dto.getGrade()).orElse(this.grade);
        this.address = Optional.ofNullable(dto.getAddress()).orElse(this.address);
        this.country = Optional.ofNullable(dto.getCountry()).orElse(this.country);
        this.termsOfUseCheck = Optional.ofNullable(dto.isTermsOfUseCheck()).orElse(this.termsOfUseCheck);
        this.privacyTermsCheck = Optional.ofNullable(dto.isPrivacyTermsCheck()).orElse(this.privacyTermsCheck);
        this.marketingTermsCheck = Optional.ofNullable(dto.isMarketingTermsCheck()).orElse(this.marketingTermsCheck);
        return this;
    }

    public void touchLastAccessAt() {
        this.lastAccessAt = LocalDateTime.now();
    }

}

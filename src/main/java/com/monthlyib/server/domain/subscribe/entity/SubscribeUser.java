package com.monthlyib.server.domain.subscribe.entity;


import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "subscribe_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeUser extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscribeUserId;

    @Column(nullable = false)
    private Long subscribeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userNickName;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int tutoringCount;

    @Column(nullable = false)
    private int subscribeMonthPeriod;

    @Column(nullable = false)
    private int videoLessonsCount;

    // 만료 기한
    @Column(nullable = false)
    private LocalDate expirationDate;

    // 구독 일자
    @Column(nullable = false)
    private LocalDate subscriptionDate;

    // 구독 날짜
    @Column(nullable = false)
    private int subscriptionDay;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> videoLessonsIdList;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscribeStatus subscribeStatus;

    public static SubscribeUser create(Subscribe subscribe, User user) {
        return SubscribeUser.builder()
                .userId(user.getUserId())
                .subscribeId(subscribe.getSubscribeId())
                .userName(user.getUsername())
                .userNickName(user.getNickName())
                .title(subscribe.getTitle())
                .content(subscribe.getContent())
                .price(subscribe.getPrice())
                .questionCount(subscribe.getQuestionCount())
                .tutoringCount(subscribe.getTutoringCount())
                .subscribeMonthPeriod(subscribe.getSubscribeMonthPeriod())
                .videoLessonsCount(subscribe.getVideoLessonsCount())
                .expirationDate(LocalDate.now().plusMonths(subscribe.getSubscribeMonthPeriod()))
                .videoLessonsIdList(new ArrayList<>())
                .subscriptionDate(LocalDate.now())
                .subscriptionDay(LocalDate.now().getDayOfMonth())
                .subscribeStatus(SubscribeStatus.WAIT)
                .build();
    }

    public SubscribeUser update(SubscribeUserPatchDto dto) {
        this.questionCount = Optional.ofNullable(dto.getQuestionCount()).orElse(this.questionCount);
        this.tutoringCount = Optional.ofNullable(dto.getTutoringCount()).orElse(this.tutoringCount);
        this.subscribeMonthPeriod = Optional.ofNullable(dto.getSubscribeMonthPeriod()).orElse(this.subscribeMonthPeriod);
        this.videoLessonsCount = Optional.ofNullable(dto.getVideoLessonsCount()).orElse(this.videoLessonsCount);
        this.videoLessonsIdList = Optional.ofNullable(dto.getVideoLessonsIdList()).orElse(this.videoLessonsIdList);
        this.subscribeStatus = Optional.ofNullable(dto.getSubscribeStatus()).orElse(this.subscribeStatus);
        return this;
    }

}

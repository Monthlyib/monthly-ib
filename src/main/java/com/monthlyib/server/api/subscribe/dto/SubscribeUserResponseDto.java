package com.monthlyib.server.api.subscribe.dto;


import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscribeUserResponseDto {

    private Long subscribeUserId;

    private Long subscribeId;

    private Long userId;

    private String userName;

    private String userNickName;

    private String title;

    private String content;

    private BigDecimal price;

    private int questionCount;

    private int tutoringCount;

    private int subscribeMonthPeriod;

    private int videoLessonsCount;

    private LocalDate expirationDate;

    // 구독 일자
    private LocalDate subscriptionDate;

    // 구독 날짜
    private int subscriptionDay;

    private List<Long> videoLessonsIdList;

    private SubscribeStatus subscribeStatus;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static SubscribeUserResponseDto of(SubscribeUser subscribeUser) {
        return SubscribeUserResponseDto.builder()
                .subscribeUserId(subscribeUser.getSubscribeUserId())
                .subscribeId(subscribeUser.getSubscribeId())
                .userId(subscribeUser.getUserId())
                .userName(subscribeUser.getUserName())
                .title(subscribeUser.getTitle())
                .content(subscribeUser.getContent())
                .price(subscribeUser.getPrice())
                .questionCount(subscribeUser.getQuestionCount())
                .tutoringCount(subscribeUser.getTutoringCount())
                .subscribeMonthPeriod(subscribeUser.getSubscribeMonthPeriod())
                .videoLessonsCount(subscribeUser.getVideoLessonsCount())
                .expirationDate(subscribeUser.getExpirationDate())
                .subscriptionDate(subscribeUser.getSubscriptionDate())
                .subscriptionDay(subscribeUser.getSubscriptionDay())
                .videoLessonsIdList(subscribeUser.getVideoLessonsIdList())
                .createAt(subscribeUser.getCreateAt())
                .updateAt(subscribeUser.getUpdateAt())
                .subscribeStatus(subscribeUser.getSubscribeStatus())
                .build();
    }
}

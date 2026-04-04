package com.monthlyib.server.api.subscribe.dto;


import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscribeResponseDto {

    private Long subscriberId;

    private String title;

    private String content;

    private BigDecimal price;

    private int questionCount;

    private boolean unlimitedQuestions;

    private int tutoringCount;

    private boolean unlimitedTutoring;

    private int subscribeMonthPeriod;

    private int videoLessonsCount;

    private boolean unlimitedVideoLessons;

    private List<Long> videoLessonsIdList;

    private String color;

    private String fontColor;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static SubscribeResponseDto of(Subscribe subscribe) {
        return SubscribeResponseDto.builder()
                .subscriberId(subscribe.getSubscribeId())
                .title(subscribe.getTitle())
                .content(subscribe.getContent())
                .price(subscribe.getPrice())
                .questionCount(subscribe.getQuestionCount())
                .unlimitedQuestions(subscribe.isUnlimitedQuestions())
                .tutoringCount(subscribe.getTutoringCount())
                .unlimitedTutoring(subscribe.isUnlimitedTutoring())
                .subscribeMonthPeriod(subscribe.getSubscribeMonthPeriod())
                .videoLessonsCount(subscribe.getVideoLessonsCount())
                .unlimitedVideoLessons(subscribe.isUnlimitedVideoLessons())
                .videoLessonsIdList(subscribe.getVideoLessonsIdList())
                .color(subscribe.getColor())
                .fontColor(subscribe.getFontColor())
                .createAt(subscribe.getCreateAt())
                .updateAt(subscribe.getUpdateAt())
                .build();
    }
}

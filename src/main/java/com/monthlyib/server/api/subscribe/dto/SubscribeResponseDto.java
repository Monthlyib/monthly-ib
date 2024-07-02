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

    private int tutoringCount;

    private int subscribeMonthPeriod;

    private int videoLessonsCount;

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
                .tutoringCount(subscribe.getTutoringCount())
                .subscribeMonthPeriod(subscribe.getSubscribeMonthPeriod())
                .videoLessonsCount(subscribe.getVideoLessonsCount())
                .videoLessonsIdList(subscribe.getVideoLessonsIdList())
                .color(subscribe.getColor())
                .fontColor(subscribe.getFontColor())
                .createAt(subscribe.getCreateAt())
                .updateAt(subscribe.getUpdateAt())
                .build();
    }
}

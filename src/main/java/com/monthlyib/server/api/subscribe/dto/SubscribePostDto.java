package com.monthlyib.server.api.subscribe.dto;


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
public class SubscribePostDto {

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

}

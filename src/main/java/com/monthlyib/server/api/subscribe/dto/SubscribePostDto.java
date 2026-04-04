package com.monthlyib.server.api.subscribe.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscribePostDto {

    private String title;

    private String content;

    private BigDecimal price;

    private Integer questionCount;

    private Boolean unlimitedQuestions;

    private Integer tutoringCount;

    private Boolean unlimitedTutoring;

    private Integer subscribeMonthPeriod;

    private Integer videoLessonsCount;

    private Boolean unlimitedVideoLessons;

    private List<Long> videoLessonsIdList;

    private String color;
    private String fontColor;

}

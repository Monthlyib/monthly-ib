package com.monthlyib.server.api.subscribe.dto;


import com.monthlyib.server.constant.SubscribeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscribeUserPatchDto {

    private Integer questionCount;

    private Integer tutoringCount;

    private Integer subscribeMonthPeriod;

    private Integer videoLessonsCount;

    private List<Long> videoLessonsIdList;

    private SubscribeStatus subscribeStatus;

}

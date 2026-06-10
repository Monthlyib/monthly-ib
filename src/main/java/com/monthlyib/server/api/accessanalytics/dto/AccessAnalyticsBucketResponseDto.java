package com.monthlyib.server.api.accessanalytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessAnalyticsBucketResponseDto {

    private String period;
    private String label;
    private String startDate;
    private String endDate;
    private long uniqueUserCount;
}

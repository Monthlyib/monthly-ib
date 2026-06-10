package com.monthlyib.server.api.accessanalytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessAnalyticsSummaryResponseDto {

    private long todayUsers;
    private long last7DaysUsers;
    private long last30DaysUsers;
    private long thisWeekUsers;
}

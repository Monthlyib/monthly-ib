package com.monthlyib.server.api.accessanalytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AccessAnalyticsDetailsResponseDto {

    private String periodType;
    private String period;
    private String startDate;
    private String endDate;
    private long uniqueUserCount;
    private List<AccessAnalyticsUserResponseDto> users;
    private LocalDateTime generatedAt;
}

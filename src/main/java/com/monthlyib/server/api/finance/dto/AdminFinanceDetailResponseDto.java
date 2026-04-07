package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminFinanceDetailResponseDto {

    private String yearMonth;
    private List<FinanceBucketResponseDto> dailyBuckets;
    private List<FinanceBreakdownResponseDto> awsBreakdown;
    private List<FinanceBreakdownResponseDto> openAiBreakdown;
    private ExchangeRateMetaResponseDto exchangeRateMeta;
    private String revenueStatus;
    private LocalDateTime lastSyncedAt;
    private String dataStatus;
    private String warningMessage;
}

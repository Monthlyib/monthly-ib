package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminFinanceOverviewResponseDto {

    private FinanceTotalsResponseDto totals;
    private List<FinanceBucketResponseDto> buckets;
    private String revenueStatus;
    private LocalDateTime lastSyncedAt;
    private String dataStatus;
    private String warningMessage;
}

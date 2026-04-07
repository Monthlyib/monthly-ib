package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinanceBucketResponseDto {

    private String period;
    private BigDecimal awsCostKrw;
    private BigDecimal openAiCostKrw;
    private BigDecimal totalOperatingCostKrw;
    private BigDecimal revenueKrw;
    private BigDecimal operatingProfitKrw;
}

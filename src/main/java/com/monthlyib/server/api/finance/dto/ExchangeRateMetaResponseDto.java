package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ExchangeRateMetaResponseDto {

    private String baseCurrency;
    private String quoteCurrency;
    private String source;
    private BigDecimal averageRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate latestRateDate;
}

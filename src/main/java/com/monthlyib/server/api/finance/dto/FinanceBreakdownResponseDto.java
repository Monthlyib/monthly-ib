package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinanceBreakdownResponseDto {

    private String label;
    private BigDecimal usdAmount;
    private BigDecimal krwAmount;
}

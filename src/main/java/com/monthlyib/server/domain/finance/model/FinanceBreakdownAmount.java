package com.monthlyib.server.domain.finance.model;

import java.math.BigDecimal;

public record FinanceBreakdownAmount(String label, BigDecimal usdAmount) {
}

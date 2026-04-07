package com.monthlyib.server.domain.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceDailyAmount(LocalDate date, BigDecimal usdAmount) {
}

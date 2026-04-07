package com.monthlyib.server.domain.finance.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public record ExchangeRateRange(Map<LocalDate, BigDecimal> rates, LocalDate latestRateDate) {

    public BigDecimal resolve(LocalDate date) {
        if (rates == null || rates.isEmpty()) {
            return null;
        }
        BigDecimal exact = rates.get(date);
        if (exact != null) {
            return exact;
        }

        Optional<Map.Entry<LocalDate, BigDecimal>> previous = rates.entrySet().stream()
                .filter(entry -> !entry.getKey().isAfter(date))
                .max(Map.Entry.comparingByKey());
        if (previous.isPresent()) {
            return previous.get().getValue();
        }

        return rates.entrySet().stream()
                .min(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public BigDecimal average(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return null;
        }

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            BigDecimal rate = resolve(cursor);
            if (rate != null) {
                total = total.add(rate);
                count += 1;
            }
            cursor = cursor.plusDays(1);
        }

        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }
}

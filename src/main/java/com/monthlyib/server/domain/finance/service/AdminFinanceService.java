package com.monthlyib.server.domain.finance.service;

import com.monthlyib.server.api.finance.dto.*;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.domain.finance.model.ExchangeRateRange;
import com.monthlyib.server.domain.finance.model.FinanceBreakdownAmount;
import com.monthlyib.server.domain.finance.model.FinanceDailyAmount;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.monthlyib.server.constant.ErrorCode.ACCESS_DENIED;

@Service
@RequiredArgsConstructor
public class AdminFinanceService {

    private static final String REVENUE_STATUS = "NOT_READY";
    private static final String EXCHANGE_SOURCE = "Frankfurter";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AwsBillingService awsBillingService;
    private final OpenAiCostService openAiCostService;
    private final ExchangeRateService exchangeRateService;

    public AdminFinanceOverviewResponseDto getOverview(User user, int months) {
        verifyAdmin(user);

        int safeMonths = Math.max(1, Math.min(months, 24));
        LocalDate startDate = YearMonth.now().minusMonths(safeMonths - 1L).atDay(1);
        LocalDate endExclusive = YearMonth.now().plusMonths(1).atDay(1);

        ProviderLoadResult<List<FinanceDailyAmount>> awsResult = awsBillingService.getDailyCosts(startDate, endExclusive);
        ProviderLoadResult<List<FinanceDailyAmount>> openAiResult = openAiCostService.getDailyCosts(startDate, endExclusive);
        ProviderLoadResult<ExchangeRateRange> exchangeResult = exchangeRateService.getUsdKrwRates(startDate, endExclusive.minusDays(1));

        Map<YearMonth, BucketAccumulator> buckets = initMonthBuckets(startDate, safeMonths);
        applyDailyAmounts(buckets, awsResult, exchangeResult, ProviderType.AWS);
        applyDailyAmounts(buckets, openAiResult, exchangeResult, ProviderType.OPENAI);

        List<FinanceBucketResponseDto> bucketResponses = buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toResponse(entry.getKey().format(YEAR_MONTH_FORMATTER)))
                .toList();

        FinanceTotalsResponseDto totals = FinanceTotalsResponseDto.builder()
                .awsCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getAwsCostKrw))
                .openAiCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getOpenAiCostKrw))
                .totalOperatingCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getTotalOperatingCostKrw))
                .revenueKrw(null)
                .operatingProfitKrw(null)
                .build();

        FinanceStatusSummary statusSummary = summarizeStatus(List.of(awsResult, openAiResult, exchangeResult));

        return AdminFinanceOverviewResponseDto.builder()
                .totals(totals)
                .buckets(bucketResponses)
                .revenueStatus(REVENUE_STATUS)
                .lastSyncedAt(statusSummary.lastSyncedAt())
                .dataStatus(statusSummary.dataStatus())
                .warningMessage(statusSummary.warningMessage())
                .build();
    }

    public AdminFinanceDetailResponseDto getDetails(User user, String yearMonthText) {
        verifyAdmin(user);

        YearMonth yearMonth = YearMonth.parse(yearMonthText, YEAR_MONTH_FORMATTER);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endExclusive = yearMonth.plusMonths(1).atDay(1);

        ProviderLoadResult<List<FinanceDailyAmount>> awsDailyResult = awsBillingService.getDailyCosts(startDate, endExclusive);
        ProviderLoadResult<List<FinanceDailyAmount>> openAiDailyResult = openAiCostService.getDailyCosts(startDate, endExclusive);
        ProviderLoadResult<List<FinanceBreakdownAmount>> awsBreakdownResult = awsBillingService.getMonthlyBreakdown(yearMonth);
        ProviderLoadResult<List<FinanceBreakdownAmount>> openAiBreakdownResult = openAiCostService.getMonthlyBreakdown(yearMonth);
        ProviderLoadResult<ExchangeRateRange> exchangeResult = exchangeRateService.getUsdKrwRates(startDate, endExclusive.minusDays(1));

        Map<LocalDate, BucketAccumulator> buckets = initDayBuckets(startDate, endExclusive.minusDays(1));
        applyDailyAmounts(buckets, awsDailyResult, exchangeResult, ProviderType.AWS);
        applyDailyAmounts(buckets, openAiDailyResult, exchangeResult, ProviderType.OPENAI);

        List<FinanceBucketResponseDto> dailyBuckets = buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toResponse(entry.getKey().toString()))
                .toList();

        ExchangeRateRange exchangeRateRange = exchangeResult.data();
        BigDecimal averageRate = exchangeRateRange == null
                ? null
                : exchangeRateRange.average(startDate, endExclusive.minusDays(1));

        List<FinanceBreakdownResponseDto> awsBreakdown = convertBreakdown(awsBreakdownResult, averageRate);
        List<FinanceBreakdownResponseDto> openAiBreakdown = convertBreakdown(openAiBreakdownResult, averageRate);

        FinanceStatusSummary statusSummary = summarizeStatus(List.of(
                awsDailyResult,
                openAiDailyResult,
                awsBreakdownResult,
                openAiBreakdownResult,
                exchangeResult
        ));

        return AdminFinanceDetailResponseDto.builder()
                .yearMonth(yearMonth.format(YEAR_MONTH_FORMATTER))
                .dailyBuckets(dailyBuckets)
                .awsBreakdown(awsBreakdown)
                .openAiBreakdown(openAiBreakdown)
                .exchangeRateMeta(ExchangeRateMetaResponseDto.builder()
                        .baseCurrency("USD")
                        .quoteCurrency("KRW")
                        .source(EXCHANGE_SOURCE)
                        .averageRate(scale(averageRate))
                        .startDate(startDate)
                        .endDate(endExclusive.minusDays(1))
                        .latestRateDate(exchangeRateRange == null ? null : exchangeRateRange.latestRateDate())
                        .build())
                .revenueStatus(REVENUE_STATUS)
                .lastSyncedAt(statusSummary.lastSyncedAt())
                .dataStatus(statusSummary.dataStatus())
                .warningMessage(statusSummary.warningMessage())
                .build();
    }

    private void verifyAdmin(User user) {
        if (user == null || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ACCESS_DENIED);
        }
    }

    private Map<YearMonth, BucketAccumulator> initMonthBuckets(LocalDate startDate, int months) {
        Map<YearMonth, BucketAccumulator> buckets = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(startDate);
        for (int i = 0; i < months; i++) {
            buckets.put(cursor, new BucketAccumulator());
            cursor = cursor.plusMonths(1);
        }
        return buckets;
    }

    private Map<LocalDate, BucketAccumulator> initDayBuckets(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BucketAccumulator> buckets = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            buckets.put(cursor, new BucketAccumulator());
            cursor = cursor.plusDays(1);
        }
        return buckets;
    }

    private void applyDailyAmounts(Map<?, BucketAccumulator> buckets,
                                   ProviderLoadResult<List<FinanceDailyAmount>> providerResult,
                                   ProviderLoadResult<ExchangeRateRange> exchangeResult,
                                   ProviderType providerType) {
        if (!providerResult.available() || !exchangeResult.available() || exchangeResult.data() == null) {
            return;
        }

        ExchangeRateRange exchangeRates = exchangeResult.data();
        for (FinanceDailyAmount amount : Optional.ofNullable(providerResult.data()).orElseGet(List::of)) {
            BucketAccumulator accumulator;
            if (buckets.keySet().stream().findFirst().orElse(null) instanceof YearMonth) {
                accumulator = buckets.get(YearMonth.from(amount.date()));
            } else {
                accumulator = buckets.get(amount.date());
            }
            if (accumulator == null) {
                continue;
            }

            BigDecimal rate = exchangeRates.resolve(amount.date());
            if (rate == null) {
                continue;
            }

            BigDecimal krw = scale(amount.usdAmount().multiply(rate));
            if (providerType == ProviderType.AWS) {
                accumulator.awsCostKrw = add(accumulator.awsCostKrw, krw);
            } else {
                accumulator.openAiCostKrw = add(accumulator.openAiCostKrw, krw);
            }
            accumulator.totalOperatingCostKrw = add(accumulator.totalOperatingCostKrw, krw);
        }
    }

    private List<FinanceBreakdownResponseDto> convertBreakdown(ProviderLoadResult<List<FinanceBreakdownAmount>> providerResult,
                                                               BigDecimal averageRate) {
        if (!providerResult.available()) {
            return List.of();
        }
        return Optional.ofNullable(providerResult.data()).orElseGet(List::of).stream()
                .map(item -> FinanceBreakdownResponseDto.builder()
                        .label(item.label())
                        .usdAmount(scale(item.usdAmount()))
                        .krwAmount(averageRate == null ? null : scale(item.usdAmount().multiply(averageRate)))
                        .build())
                .toList();
    }

    private FinanceStatusSummary summarizeStatus(List<ProviderLoadResult<?>> results) {
        boolean hasUnavailable = false;
        boolean hasStale = false;
        Set<String> warnings = new LinkedHashSet<>();
        LocalDateTime lastSyncedAt = results.stream()
                .map(ProviderLoadResult::fetchedAt)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(null);

        for (ProviderLoadResult<?> result : results) {
            if (result == null) {
                continue;
            }
            if (!result.available()) {
                hasUnavailable = true;
            }
            if (result.stale()) {
                hasStale = true;
            }
            if (result.warningMessage() != null && !result.warningMessage().isBlank()) {
                warnings.add(result.warningMessage());
            }
        }

        String dataStatus;
        if (hasUnavailable && lastSyncedAt == null) {
            dataStatus = "FAILED";
        } else if (hasUnavailable) {
            dataStatus = "PARTIAL";
        } else if (hasStale) {
            dataStatus = "STALE";
        } else {
            dataStatus = "LIVE";
        }

        return new FinanceStatusSummary(
                dataStatus,
                warnings.isEmpty() ? null : String.join(" ", warnings),
                lastSyncedAt
        );
    }

    private BigDecimal sum(List<FinanceBucketResponseDto> buckets, AmountExtractor extractor) {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;
        for (FinanceBucketResponseDto bucket : buckets) {
            BigDecimal value = extractor.extract(bucket);
            if (value != null) {
                total = total.add(value);
                hasValue = true;
            }
        }
        return hasValue ? scale(total) : null;
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        if (left == null) {
            return scale(right);
        }
        if (right == null) {
            return scale(left);
        }
        return scale(left.add(right));
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    @FunctionalInterface
    private interface AmountExtractor {
        BigDecimal extract(FinanceBucketResponseDto bucket);
    }

    private enum ProviderType {
        AWS,
        OPENAI
    }

    private static class BucketAccumulator {
        private BigDecimal awsCostKrw;
        private BigDecimal openAiCostKrw;
        private BigDecimal totalOperatingCostKrw;

        private FinanceBucketResponseDto toResponse(String period) {
            return FinanceBucketResponseDto.builder()
                    .period(period)
                    .awsCostKrw(awsCostKrw)
                    .openAiCostKrw(openAiCostKrw)
                    .totalOperatingCostKrw(totalOperatingCostKrw)
                    .revenueKrw(null)
                    .operatingProfitKrw(null)
                    .build();
        }
    }

    private record FinanceStatusSummary(String dataStatus, String warningMessage, LocalDateTime lastSyncedAt) {
    }
}

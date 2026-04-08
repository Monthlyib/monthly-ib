package com.monthlyib.server.domain.finance.service;

import com.monthlyib.server.api.finance.dto.AdminFinanceDetailResponseDto;
import com.monthlyib.server.api.finance.dto.AdminFinanceOverviewResponseDto;
import com.monthlyib.server.api.finance.dto.ExchangeRateMetaResponseDto;
import com.monthlyib.server.api.finance.dto.FinanceBreakdownResponseDto;
import com.monthlyib.server.api.finance.dto.FinanceBucketResponseDto;
import com.monthlyib.server.api.finance.dto.FinanceTotalsResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.domain.finance.entity.AdminFinanceDailyBreakdown;
import com.monthlyib.server.domain.finance.entity.AdminFinanceDailySnapshot;
import com.monthlyib.server.domain.finance.entity.AdminFinanceSyncJob;
import com.monthlyib.server.domain.finance.entity.FinanceJobStatus;
import com.monthlyib.server.domain.finance.entity.FinanceProvider;
import com.monthlyib.server.domain.finance.entity.FinanceSyncStatus;
import com.monthlyib.server.domain.finance.repository.AdminFinanceDailyBreakdownJpaRepository;
import com.monthlyib.server.domain.finance.repository.AdminFinanceDailySnapshotJpaRepository;
import com.monthlyib.server.domain.finance.repository.AdminFinanceSyncJobJpaRepository;
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
import java.util.function.Function;

import static com.monthlyib.server.constant.ErrorCode.ACCESS_DENIED;

@Service
@RequiredArgsConstructor
public class AdminFinanceService {

    private static final String REVENUE_STATUS = "LEDGER_ONLY";
    private static final String EXCHANGE_SOURCE = "StoredDailySnapshot";
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AdminFinanceDailySnapshotJpaRepository snapshotRepository;
    private final AdminFinanceDailyBreakdownJpaRepository breakdownRepository;
    private final AdminFinanceSyncJobJpaRepository syncJobRepository;

    public AdminFinanceOverviewResponseDto getOverview(User user, int months) {
        verifyAdmin(user);

        int safeMonths = Math.max(1, Math.min(months, 24));
        LocalDate startDate = YearMonth.now().minusMonths(safeMonths - 1L).atDay(1);
        LocalDate endDate = LocalDate.now();

        List<AdminFinanceDailySnapshot> snapshots =
                snapshotRepository.findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);

        Map<YearMonth, BucketAccumulator> buckets = initMonthBuckets(startDate, safeMonths);
        for (AdminFinanceDailySnapshot snapshot : snapshots) {
            BucketAccumulator accumulator = buckets.get(YearMonth.from(snapshot.getSnapshotDate()));
            if (accumulator == null) {
                continue;
            }
            accumulator.addSnapshot(snapshot);
        }

        List<FinanceBucketResponseDto> bucketResponses = buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toResponse(entry.getKey().format(YEAR_MONTH_FORMATTER)))
                .toList();

        FinanceTotalsResponseDto totals = FinanceTotalsResponseDto.builder()
                .awsCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getAwsCostKrw))
                .openAiCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getOpenAiCostKrw))
                .totalOperatingCostKrw(sum(bucketResponses, FinanceBucketResponseDto::getTotalOperatingCostKrw))
                .revenueKrw(sum(bucketResponses, FinanceBucketResponseDto::getRevenueKrw))
                .operatingProfitKrw(sum(bucketResponses, FinanceBucketResponseDto::getOperatingProfitKrw))
                .build();

        FinanceStatusSummary statusSummary = summarizeStatus(snapshots);

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
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AdminFinanceDailySnapshot> snapshots =
                snapshotRepository.findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);
        List<AdminFinanceDailyBreakdown> breakdowns =
                breakdownRepository.findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(startDate, endDate);

        Map<LocalDate, BucketAccumulator> dailyBuckets = initDayBuckets(startDate, endDate);
        for (AdminFinanceDailySnapshot snapshot : snapshots) {
            BucketAccumulator accumulator = dailyBuckets.get(snapshot.getSnapshotDate());
            if (accumulator != null) {
                accumulator.addSnapshot(snapshot);
            }
        }

        List<FinanceBucketResponseDto> dailyBucketResponses = dailyBuckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toResponse(entry.getKey().toString()))
                .toList();

        ExchangeRateMetaResponseDto exchangeRateMeta = buildExchangeMeta(startDate, endDate, snapshots);
        List<FinanceBreakdownResponseDto> awsBreakdown = aggregateBreakdowns(breakdowns, FinanceProvider.AWS);
        List<FinanceBreakdownResponseDto> openAiBreakdown = aggregateBreakdowns(breakdowns, FinanceProvider.OPENAI);
        List<FinanceBreakdownResponseDto> revenueBreakdown = aggregateBreakdowns(breakdowns, FinanceProvider.REVENUE);

        FinanceStatusSummary statusSummary = summarizeStatus(snapshots);

        return AdminFinanceDetailResponseDto.builder()
                .yearMonth(yearMonth.format(YEAR_MONTH_FORMATTER))
                .dailyBuckets(dailyBucketResponses)
                .awsBreakdown(awsBreakdown)
                .openAiBreakdown(openAiBreakdown)
                .revenueBreakdown(revenueBreakdown)
                .exchangeRateMeta(exchangeRateMeta)
                .revenueStatus(REVENUE_STATUS)
                .lastSyncedAt(statusSummary.lastSyncedAt())
                .dataStatus(statusSummary.dataStatus())
                .warningMessage(statusSummary.warningMessage())
                .build();
    }

    private ExchangeRateMetaResponseDto buildExchangeMeta(LocalDate startDate,
                                                          LocalDate endDate,
                                                          List<AdminFinanceDailySnapshot> snapshots) {
        List<BigDecimal> rates = snapshots.stream()
                .map(AdminFinanceDailySnapshot::getUsdKrwRate)
                .filter(value -> value != null)
                .toList();

        BigDecimal averageRate = null;
        if (!rates.isEmpty()) {
            BigDecimal total = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            averageRate = total.divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);
        }

        LocalDate latestRateDate = snapshots.stream()
                .filter(snapshot -> snapshot.getUsdKrwRate() != null)
                .map(AdminFinanceDailySnapshot::getSnapshotDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return ExchangeRateMetaResponseDto.builder()
                .baseCurrency("USD")
                .quoteCurrency("KRW")
                .source(EXCHANGE_SOURCE)
                .averageRate(averageRate)
                .startDate(startDate)
                .endDate(endDate)
                .latestRateDate(latestRateDate)
                .build();
    }

    private List<FinanceBreakdownResponseDto> aggregateBreakdowns(List<AdminFinanceDailyBreakdown> breakdowns, FinanceProvider provider) {
        Map<String, BreakdownAccumulator> aggregated = new LinkedHashMap<>();
        for (AdminFinanceDailyBreakdown breakdown : breakdowns) {
            if (breakdown.getProvider() != provider) {
                continue;
            }
            BreakdownAccumulator accumulator = aggregated.computeIfAbsent(
                    breakdown.getBreakdownKey(),
                    ignored -> new BreakdownAccumulator()
            );
            accumulator.amountUsd = add(accumulator.amountUsd, breakdown.getAmountUsd());
            accumulator.amountKrw = add(accumulator.amountKrw, breakdown.getAmountKrw());
        }

        return aggregated.entrySet().stream()
                .sorted((left, right) -> right.getValue().amountKrw.compareTo(left.getValue().amountKrw))
                .map(entry -> FinanceBreakdownResponseDto.builder()
                        .label(mapBreakdownLabel(provider, entry.getKey()))
                        .usdAmount(isZero(entry.getValue().amountUsd) ? null : scaleUsd(entry.getValue().amountUsd))
                        .krwAmount(isZero(entry.getValue().amountKrw) ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : scale(entry.getValue().amountKrw))
                        .build())
                .toList();
    }

    private String mapBreakdownLabel(FinanceProvider provider, String key) {
        if (provider != FinanceProvider.REVENUE) {
            return key;
        }
        if ("SUBSCRIBE:CONFIRMED".equals(key)) {
            return "구독 결제";
        }
        if ("SUBSCRIBE:REFUNDED".equals(key)) {
            return "구독 환불";
        }
        return key;
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

    private FinanceStatusSummary summarizeStatus(List<AdminFinanceDailySnapshot> snapshots) {
        Optional<AdminFinanceSyncJob> latestFinishedJob = syncJobRepository.findTopByFinishedAtIsNotNullOrderByFinishedAtDesc();

        if (snapshots.isEmpty()) {
            return new FinanceStatusSummary(
                    "FAILED",
                    latestFinishedJob.map(AdminFinanceSyncJob::getWarningMessage)
                            .filter(message -> message != null && !message.isBlank())
                            .orElse("동기화된 운영 수익 데이터가 없습니다."),
                    latestFinishedJob.map(AdminFinanceSyncJob::getFinishedAt).orElse(null)
            );
        }

        boolean hasFailure = snapshots.stream().anyMatch(this::hasProviderFailure);
        boolean latestJobPartial = latestFinishedJob
                .map(AdminFinanceSyncJob::getJobStatus)
                .map(status -> status == FinanceJobStatus.PARTIAL || status == FinanceJobStatus.FAILED)
                .orElse(false);
        LocalDateTime lastSyncedAt = snapshots.stream()
                .map(AdminFinanceDailySnapshot::getSyncedAt)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(latestFinishedJob.map(AdminFinanceSyncJob::getFinishedAt).orElse(null));

        String dataStatus;
        if (hasFailure || latestJobPartial) {
            dataStatus = "PARTIAL";
        } else if (lastSyncedAt != null && lastSyncedAt.isBefore(LocalDateTime.now().minusHours(36))) {
            dataStatus = "STALE";
        } else {
            dataStatus = "LIVE";
        }

        Set<String> warnings = new LinkedHashSet<>();
        for (AdminFinanceDailySnapshot snapshot : snapshots) {
            if (snapshot.getWarningMessage() != null && !snapshot.getWarningMessage().isBlank()) {
                warnings.add(snapshot.getWarningMessage());
            }
        }
        latestFinishedJob.map(AdminFinanceSyncJob::getWarningMessage)
                .filter(message -> message != null && !message.isBlank())
                .ifPresent(warnings::add);

        return new FinanceStatusSummary(
                dataStatus,
                warnings.isEmpty() ? null : String.join(" ", warnings),
                lastSyncedAt
        );
    }

    private boolean hasProviderFailure(AdminFinanceDailySnapshot snapshot) {
        return snapshot.getAwsStatus() == FinanceSyncStatus.FAILED
                || snapshot.getOpenAiStatus() == FinanceSyncStatus.FAILED
                || snapshot.getRevenueStatus() == FinanceSyncStatus.FAILED;
    }

    private BigDecimal sum(List<FinanceBucketResponseDto> buckets, Function<FinanceBucketResponseDto, BigDecimal> extractor) {
        BigDecimal total = BigDecimal.ZERO;
        for (FinanceBucketResponseDto bucket : buckets) {
            total = total.add(Optional.ofNullable(extractor.apply(bucket)).orElse(BigDecimal.ZERO));
        }
        return scale(total);
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        return Optional.ofNullable(left).orElse(BigDecimal.ZERO)
                .add(Optional.ofNullable(right).orElse(BigDecimal.ZERO));
    }

    private boolean isZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal scale(BigDecimal value) {
        return Optional.ofNullable(value)
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleUsd(BigDecimal value) {
        return Optional.ofNullable(value)
                .orElse(BigDecimal.ZERO)
                .setScale(6, RoundingMode.HALF_UP);
    }

    private void verifyAdmin(User user) {
        if (user == null || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ACCESS_DENIED);
        }
    }

    private record FinanceStatusSummary(String dataStatus, String warningMessage, LocalDateTime lastSyncedAt) {
    }

    private static final class BreakdownAccumulator {
        private BigDecimal amountUsd = BigDecimal.ZERO;
        private BigDecimal amountKrw = BigDecimal.ZERO;
    }

    private static final class BucketAccumulator {
        private BigDecimal awsCostKrw = BigDecimal.ZERO;
        private BigDecimal openAiCostKrw = BigDecimal.ZERO;
        private BigDecimal totalOperatingCostKrw = BigDecimal.ZERO;
        private BigDecimal revenueKrw = BigDecimal.ZERO;
        private BigDecimal operatingProfitKrw = BigDecimal.ZERO;

        private void addSnapshot(AdminFinanceDailySnapshot snapshot) {
            awsCostKrw = add(awsCostKrw, snapshot.getAwsCostKrw());
            openAiCostKrw = add(openAiCostKrw, snapshot.getOpenAiCostKrw());
            totalOperatingCostKrw = add(totalOperatingCostKrw, snapshot.getTotalOperatingCostKrw());
            revenueKrw = add(revenueKrw, snapshot.getRevenueKrw());
            operatingProfitKrw = add(operatingProfitKrw, snapshot.getOperatingProfitKrw());
        }

        private FinanceBucketResponseDto toResponse(String period) {
            return FinanceBucketResponseDto.builder()
                    .period(period)
                    .awsCostKrw(scale(awsCostKrw))
                    .openAiCostKrw(scale(openAiCostKrw))
                    .totalOperatingCostKrw(scale(totalOperatingCostKrw))
                    .revenueKrw(scale(revenueKrw))
                    .operatingProfitKrw(scale(operatingProfitKrw))
                    .build();
        }

        private BigDecimal add(BigDecimal left, BigDecimal right) {
            return Optional.ofNullable(left).orElse(BigDecimal.ZERO)
                    .add(Optional.ofNullable(right).orElse(BigDecimal.ZERO));
        }

        private BigDecimal scale(BigDecimal value) {
            return Optional.ofNullable(value)
                    .orElse(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }
}

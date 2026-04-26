package com.monthlyib.server.domain.finance.service;

import com.monthlyib.server.api.finance.dto.FinanceSyncJobResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.domain.finance.entity.AdminFinanceDailyBreakdown;
import com.monthlyib.server.domain.finance.entity.AdminFinanceDailySnapshot;
import com.monthlyib.server.domain.finance.entity.AdminFinanceSyncJob;
import com.monthlyib.server.domain.finance.entity.FinanceJobStatus;
import com.monthlyib.server.domain.finance.entity.FinanceProvider;
import com.monthlyib.server.domain.finance.entity.FinanceSyncStatus;
import com.monthlyib.server.domain.finance.entity.FinanceSyncTriggerType;
import com.monthlyib.server.domain.finance.entity.PaymentLedgerEvent;
import com.monthlyib.server.domain.finance.entity.PaymentLedgerEventType;
import com.monthlyib.server.domain.finance.model.ExchangeRateRange;
import com.monthlyib.server.domain.finance.model.FinanceBreakdownAmount;
import com.monthlyib.server.domain.finance.model.FinanceDailyAmount;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import com.monthlyib.server.domain.finance.repository.AdminFinanceDailyBreakdownJpaRepository;
import com.monthlyib.server.domain.finance.repository.AdminFinanceDailySnapshotJpaRepository;
import com.monthlyib.server.domain.finance.repository.AdminFinanceSyncJobJpaRepository;
import com.monthlyib.server.domain.finance.repository.PaymentLedgerEventJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.monthlyib.server.constant.ErrorCode.ACCESS_DENIED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFinanceSnapshotSyncService {

    private static final int DEFAULT_SYNC_MONTHS = 12;
    private static final String REVENUE_BREAKDOWN_CONFIRMED = "SUBSCRIBE:CONFIRMED";
    private static final String REVENUE_BREAKDOWN_REFUNDED = "SUBSCRIBE:REFUNDED";

    private final AwsBillingService awsBillingService;
    private final OpenAiCostService openAiCostService;
    private final ExchangeRateService exchangeRateService;
    private final AdminFinanceDailySnapshotJpaRepository snapshotRepository;
    private final AdminFinanceDailyBreakdownJpaRepository breakdownRepository;
    private final AdminFinanceSyncJobJpaRepository syncJobRepository;
    private final PaymentLedgerEventJpaRepository paymentLedgerEventRepository;
    @Qualifier("financeSyncExecutor")
    private final Executor financeSyncExecutor;

    private final AtomicBoolean syncRunning = new AtomicBoolean(false);

    public FinanceSyncJobResponseDto triggerManualSync(User user) {
        verifyAdmin(user);
        SyncWindow window = buildDefaultWindow();
        return queueSync(window.startDate(), window.endDate(), FinanceSyncTriggerType.MANUAL);
    }

    public void triggerInitialBackfillIfNeeded() {
        if (snapshotRepository.count() > 0) {
            return;
        }
        SyncWindow window = buildDefaultWindow();
        queueSync(window.startDate(), window.endDate(), FinanceSyncTriggerType.STARTUP);
    }

    public void triggerScheduledDailySync() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        queueSync(targetDate, targetDate, FinanceSyncTriggerType.SCHEDULED);
    }

    private FinanceSyncJobResponseDto queueSync(LocalDate startDate, LocalDate endDate, FinanceSyncTriggerType triggerType) {
        Optional<AdminFinanceSyncJob> runningJob = syncJobRepository.findTopByJobStatusOrderByStartedAtDesc(FinanceJobStatus.RUNNING);
        if (!syncRunning.compareAndSet(false, true)) {
            return runningJob
                    .map(job -> toJobResponse(job, "이미 운영 수익 동기화가 진행 중입니다."))
                    .orElseGet(() -> FinanceSyncJobResponseDto.builder()
                            .jobStatus(FinanceJobStatus.RUNNING.name())
                            .triggerType(triggerType.name())
                            .periodStart(startDate)
                            .periodEnd(endDate)
                            .message("이미 운영 수익 동기화가 진행 중입니다.")
                            .build());
        }

        AdminFinanceSyncJob job = syncJobRepository.save(AdminFinanceSyncJob.builder()
                .triggerType(triggerType)
                .jobStatus(FinanceJobStatus.RUNNING)
                .periodStart(startDate)
                .periodEnd(endDate)
                .startedAt(LocalDateTime.now())
                .build());

        financeSyncExecutor.execute(() -> runSync(job.getAdminFinanceSyncJobId(), startDate, endDate));
        return toJobResponse(job, "운영 수익 동기화를 시작했습니다.");
    }

    private void runSync(Long jobId, LocalDate startDate, LocalDate endDate) {
        FinanceJobStatus finalStatus = FinanceJobStatus.SUCCESS;
        Set<String> warnings = new LinkedHashSet<>();

        try {
            ProviderLoadResult<ExchangeRateRange> exchangeRateResult = exchangeRateService.loadUsdKrwRates(startDate, endDate);
            if (!exchangeRateResult.available()) {
                warnings.add(exchangeRateResult.warningMessage());
                finalStatus = FinanceJobStatus.PARTIAL;
            } else if (hasWarning(exchangeRateResult)) {
                warnings.add(exchangeRateResult.warningMessage());
                finalStatus = FinanceJobStatus.PARTIAL;
            }

            Map<LocalDate, RevenueDailyAggregation> revenueByDate = buildRevenueMap(startDate, endDate);

            YearMonth cursor = YearMonth.from(startDate);
            YearMonth lastMonth = YearMonth.from(endDate);
            while (!cursor.isAfter(lastMonth)) {
                LocalDate monthStart = cursor.atDay(1).isBefore(startDate) ? startDate : cursor.atDay(1);
                LocalDate monthEnd = cursor.atEndOfMonth().isAfter(endDate) ? endDate : cursor.atEndOfMonth();
                MonthSyncResult monthSyncResult = syncMonth(monthStart, monthEnd, exchangeRateResult, revenueByDate);
                warnings.addAll(monthSyncResult.warnings());
                if (monthSyncResult.partial()) {
                    finalStatus = FinanceJobStatus.PARTIAL;
                }
                cursor = cursor.plusMonths(1);
            }
        } catch (Exception exception) {
            log.error("Admin finance sync failed", exception);
            warnings.add("운영 수익 동기화에 실패했습니다.");
            finalStatus = FinanceJobStatus.FAILED;
        } finally {
            syncRunning.set(false);
            AdminFinanceSyncJob job = syncJobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setJobStatus(finalStatus);
                job.setWarningMessage(warnings.isEmpty() ? null : String.join(" ", warnings));
                job.setFinishedAt(LocalDateTime.now());
                syncJobRepository.save(job);
            }
        }
    }

    private MonthSyncResult syncMonth(LocalDate startDate,
                                      LocalDate endDate,
                                      ProviderLoadResult<ExchangeRateRange> exchangeRateResult,
                                      Map<LocalDate, RevenueDailyAggregation> revenueByDate) {
        LocalDate endExclusive = endDate.plusDays(1);
        ProviderLoadResult<List<FinanceDailyAmount>> awsDailyResult = awsBillingService.loadDailyCosts(startDate, endExclusive);
        ProviderLoadResult<Map<LocalDate, List<FinanceBreakdownAmount>>> awsBreakdownResult = awsBillingService.loadDailyBreakdown(startDate, endExclusive);
        ProviderLoadResult<List<FinanceDailyAmount>> openAiDailyResult = openAiCostService.loadDailyCosts(startDate, endExclusive);
        ProviderLoadResult<Map<LocalDate, List<FinanceBreakdownAmount>>> openAiBreakdownResult = openAiCostService.loadDailyBreakdown(startDate, endExclusive);

        Set<String> warnings = new LinkedHashSet<>();
        boolean partial = false;

        if (!awsDailyResult.available() || !awsBreakdownResult.available()) {
            warnings.add("AWS 비용 데이터를 불러오지 못했습니다.");
            partial = true;
        }
        if (!openAiDailyResult.available() || !openAiBreakdownResult.available()) {
            warnings.add("OpenAI 비용 데이터를 불러오지 못했습니다.");
            partial = true;
        }
        if (!exchangeRateResult.available()) {
            warnings.add(exchangeRateResult.warningMessage());
            partial = true;
        } else if (hasWarning(exchangeRateResult)) {
            warnings.add(exchangeRateResult.warningMessage());
            partial = true;
        }

        Map<LocalDate, BigDecimal> awsDailyAmounts = toDailyAmountMap(awsDailyResult.data());
        Map<LocalDate, BigDecimal> openAiDailyAmounts = toDailyAmountMap(openAiDailyResult.data());
        Map<LocalDate, List<FinanceBreakdownAmount>> awsBreakdowns = Optional.ofNullable(awsBreakdownResult.data()).orElseGet(Map::of);
        Map<LocalDate, List<FinanceBreakdownAmount>> openAiBreakdowns = Optional.ofNullable(openAiBreakdownResult.data()).orElseGet(Map::of);
        ExchangeRateRange exchangeRateRange = exchangeRateResult.data();
        LocalDateTime syncedAt = LocalDateTime.now();

        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            LocalDate currentDate = cursor;
            AdminFinanceDailySnapshot snapshot = snapshotRepository.findById(currentDate)
                    .orElseGet(() -> AdminFinanceDailySnapshot.builder()
                            .snapshotDate(currentDate)
                            .awsStatus(FinanceSyncStatus.FAILED)
                            .openAiStatus(FinanceSyncStatus.FAILED)
                            .revenueStatus(FinanceSyncStatus.SYNCED)
                            .build());

            BigDecimal rate = exchangeRateRange == null ? null : exchangeRateRange.resolve(currentDate);
            if (rate != null) {
                snapshot.setUsdKrwRate(scaleRate(rate));
            }

            List<String> dayWarnings = new ArrayList<>();

            applyProviderCost(snapshot, FinanceProvider.AWS, awsDailyResult.available(), awsDailyAmounts.get(currentDate), rate, dayWarnings);
            applyProviderCost(snapshot, FinanceProvider.OPENAI, openAiDailyResult.available(), openAiDailyAmounts.get(currentDate), rate, dayWarnings);
            applyRevenue(snapshot, revenueByDate.get(currentDate));

            snapshot.setTotalOperatingCostKrw(scale(add(snapshot.getAwsCostKrw(), snapshot.getOpenAiCostKrw())));
            snapshot.setOperatingProfitKrw(scale(subtract(snapshot.getRevenueKrw(), snapshot.getTotalOperatingCostKrw())));
            snapshot.setWarningMessage(dayWarnings.isEmpty() ? null : String.join(" ", dayWarnings));
            snapshot.setSyncedAt(syncedAt);
            snapshotRepository.save(snapshot);

            if (awsBreakdownResult.available()) {
                replaceBreakdowns(currentDate, FinanceProvider.AWS, awsBreakdowns.get(currentDate), rate);
            }
            if (openAiBreakdownResult.available()) {
                replaceBreakdowns(currentDate, FinanceProvider.OPENAI, openAiBreakdowns.get(currentDate), rate);
            }
            replaceRevenueBreakdowns(currentDate, revenueByDate.get(currentDate));

            cursor = cursor.plusDays(1);
        }

        return new MonthSyncResult(partial, warnings);
    }

    private void applyProviderCost(AdminFinanceDailySnapshot snapshot,
                                   FinanceProvider provider,
                                   boolean available,
                                   BigDecimal usdAmount,
                                   BigDecimal rate,
                                   List<String> dayWarnings) {
        if (!available) {
            dayWarnings.add(provider == FinanceProvider.AWS
                    ? "AWS 비용 동기화에 실패했습니다."
                    : "OpenAI 비용 동기화에 실패했습니다.");
            updateProviderStatus(snapshot, provider, FinanceSyncStatus.FAILED);
            return;
        }

        BigDecimal safeUsd = scaleUsd(usdAmount == null ? BigDecimal.ZERO : usdAmount);
        BigDecimal krwAmount = rate == null ? null : scale(safeUsd.multiply(rate));
        if (provider == FinanceProvider.AWS) {
            snapshot.setAwsCostUsd(safeUsd);
            if (krwAmount != null) {
                snapshot.setAwsCostKrw(krwAmount);
            }
        } else {
            snapshot.setOpenAiCostUsd(safeUsd);
            if (krwAmount != null) {
                snapshot.setOpenAiCostKrw(krwAmount);
            }
        }

        if (rate == null) {
            dayWarnings.add("환율 데이터가 없어 일부 비용을 원화로 환산하지 못했습니다.");
            updateProviderStatus(snapshot, provider, FinanceSyncStatus.FAILED);
            return;
        }

        updateProviderStatus(snapshot, provider, FinanceSyncStatus.SYNCED);
    }

    private void applyRevenue(AdminFinanceDailySnapshot snapshot, RevenueDailyAggregation revenueDailyAggregation) {
        BigDecimal revenueAmount = revenueDailyAggregation == null
                ? BigDecimal.ZERO
                : revenueDailyAggregation.totalKrw();
        snapshot.setRevenueKrw(scale(revenueAmount));
        snapshot.setRevenueStatus(FinanceSyncStatus.SYNCED);
    }

    private void replaceBreakdowns(LocalDate snapshotDate,
                                   FinanceProvider provider,
                                   List<FinanceBreakdownAmount> breakdownAmounts,
                                   BigDecimal rate) {
        breakdownRepository.deleteAllBySnapshotDateAndProvider(snapshotDate, provider);
        if (breakdownAmounts == null || breakdownAmounts.isEmpty()) {
            return;
        }

        List<AdminFinanceDailyBreakdown> rows = breakdownAmounts.stream()
                .map(item -> AdminFinanceDailyBreakdown.builder()
                        .snapshotDate(snapshotDate)
                        .provider(provider)
                        .breakdownKey(item.label())
                        .amountUsd(scaleUsd(item.usdAmount()))
                        .amountKrw(rate == null ? null : scale(scaleUsd(item.usdAmount()).multiply(rate)))
                        .build())
                .toList();
        breakdownRepository.saveAll(rows);
    }

    private void replaceRevenueBreakdowns(LocalDate snapshotDate, RevenueDailyAggregation revenueDailyAggregation) {
        breakdownRepository.deleteAllBySnapshotDateAndProvider(snapshotDate, FinanceProvider.REVENUE);
        if (revenueDailyAggregation == null || revenueDailyAggregation.breakdownKrw().isEmpty()) {
            return;
        }

        List<AdminFinanceDailyBreakdown> rows = revenueDailyAggregation.breakdownKrw().entrySet().stream()
                .map(entry -> AdminFinanceDailyBreakdown.builder()
                        .snapshotDate(snapshotDate)
                        .provider(FinanceProvider.REVENUE)
                        .breakdownKey(entry.getKey())
                        .amountUsd(null)
                        .amountKrw(scale(entry.getValue()))
                        .build())
                .toList();
        breakdownRepository.saveAll(rows);
    }

    private Map<LocalDate, RevenueDailyAggregation> buildRevenueMap(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, RevenueDailyAggregation> revenueByDate = new LinkedHashMap<>();
        for (PaymentLedgerEvent event : paymentLedgerEventRepository.findAllByEventDateBetweenOrderByEventDateAsc(startDate, endDate)) {
            RevenueDailyAggregation aggregation = revenueByDate.computeIfAbsent(
                    event.getEventDate(),
                    ignored -> new RevenueDailyAggregation(BigDecimal.ZERO, new LinkedHashMap<>())
            );
            BigDecimal amount = Optional.ofNullable(event.getAmountKrw()).orElse(BigDecimal.ZERO);
            if (event.getEventType() == PaymentLedgerEventType.CONFIRMED) {
                aggregation.totalKrw = aggregation.totalKrw.add(amount);
                aggregation.breakdownKrw.merge(REVENUE_BREAKDOWN_CONFIRMED, amount, BigDecimal::add);
            } else if (event.getEventType() == PaymentLedgerEventType.REFUNDED) {
                aggregation.totalKrw = aggregation.totalKrw.subtract(amount);
                aggregation.breakdownKrw.merge(REVENUE_BREAKDOWN_REFUNDED, amount.negate(), BigDecimal::add);
            }
        }
        return revenueByDate;
    }

    private Map<LocalDate, BigDecimal> toDailyAmountMap(List<FinanceDailyAmount> values) {
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        if (values == null) {
            return result;
        }
        for (FinanceDailyAmount value : values) {
            result.put(value.date(), value.usdAmount());
        }
        return result;
    }

    private void updateProviderStatus(AdminFinanceDailySnapshot snapshot, FinanceProvider provider, FinanceSyncStatus status) {
        if (provider == FinanceProvider.AWS) {
            snapshot.setAwsStatus(status);
        } else if (provider == FinanceProvider.OPENAI) {
            snapshot.setOpenAiStatus(status);
        } else {
            snapshot.setRevenueStatus(status);
        }
    }

    private boolean hasWarning(ProviderLoadResult<?> result) {
        return result.warningMessage() != null && !result.warningMessage().isBlank();
    }

    private SyncWindow buildDefaultWindow() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = YearMonth.from(endDate).minusMonths(DEFAULT_SYNC_MONTHS - 1L).atDay(1);
        return new SyncWindow(startDate, endDate);
    }

    private FinanceSyncJobResponseDto toJobResponse(AdminFinanceSyncJob job, String message) {
        return FinanceSyncJobResponseDto.builder()
                .syncJobId(job.getAdminFinanceSyncJobId())
                .jobStatus(job.getJobStatus().name())
                .triggerType(job.getTriggerType().name())
                .periodStart(job.getPeriodStart())
                .periodEnd(job.getPeriodEnd())
                .startedAt(job.getStartedAt())
                .message(message)
                .build();
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

    private BigDecimal scaleRate(BigDecimal value) {
        return Optional.ofNullable(value)
                .orElse(BigDecimal.ZERO)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal add(BigDecimal left, BigDecimal right) {
        return Optional.ofNullable(left).orElse(BigDecimal.ZERO)
                .add(Optional.ofNullable(right).orElse(BigDecimal.ZERO));
    }

    private BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return Optional.ofNullable(left).orElse(BigDecimal.ZERO)
                .subtract(Optional.ofNullable(right).orElse(BigDecimal.ZERO));
    }

    private void verifyAdmin(User user) {
        if (user == null || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ACCESS_DENIED);
        }
    }

    private record SyncWindow(LocalDate startDate, LocalDate endDate) {
    }

    private record MonthSyncResult(boolean partial, Set<String> warnings) {
    }

    private static final class RevenueDailyAggregation {
        private BigDecimal totalKrw;
        private final Map<String, BigDecimal> breakdownKrw;

        private RevenueDailyAggregation(BigDecimal totalKrw, Map<String, BigDecimal> breakdownKrw) {
            this.totalKrw = totalKrw;
            this.breakdownKrw = breakdownKrw;
        }

        private BigDecimal totalKrw() {
            return totalKrw;
        }

        private Map<String, BigDecimal> breakdownKrw() {
            return breakdownKrw;
        }
    }
}

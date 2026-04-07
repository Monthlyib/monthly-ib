package com.monthlyib.server.domain.finance.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.DateInterval;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageRequest;
import com.amazonaws.services.costexplorer.model.GetCostAndUsageResult;
import com.amazonaws.services.costexplorer.model.Granularity;
import com.amazonaws.services.costexplorer.model.Group;
import com.amazonaws.services.costexplorer.model.GroupDefinition;
import com.amazonaws.services.costexplorer.model.MetricValue;
import com.amazonaws.services.costexplorer.model.ResultByTime;
import com.monthlyib.server.domain.finance.model.FinanceBreakdownAmount;
import com.monthlyib.server.domain.finance.model.FinanceDailyAmount;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsBillingService {

    @Value("${finance.aws.access-key-id:}")
    private String accessKeyId;

    @Value("${finance.aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${finance.aws.region:us-east-1}")
    private String region;

    private final FinanceCacheService financeCacheService;

    public ProviderLoadResult<List<FinanceDailyAmount>> getDailyCosts(LocalDate startDate, LocalDate endExclusive) {
        return financeCacheService.getOrLoad(
                buildDailyKey(startDate, endExclusive),
                "AWS 비용",
                () -> fetchDailyCosts(startDate, endExclusive)
        );
    }

    public ProviderLoadResult<List<FinanceBreakdownAmount>> getMonthlyBreakdown(YearMonth yearMonth) {
        return financeCacheService.getOrLoad(
                buildBreakdownKey(yearMonth),
                "AWS 서비스별 비용",
                () -> fetchMonthlyBreakdown(yearMonth)
        );
    }

    private List<FinanceDailyAmount> fetchDailyCosts(LocalDate startDate, LocalDate endExclusive) {
        validateConfigured();

        List<FinanceDailyAmount> results = new ArrayList<>();
        String nextPageToken = null;
        AWSCostExplorer client = createClient();
        try {
            do {
                GetCostAndUsageRequest request = new GetCostAndUsageRequest()
                        .withTimePeriod(new DateInterval()
                                .withStart(startDate.toString())
                                .withEnd(endExclusive.toString()))
                        .withGranularity(Granularity.DAILY)
                        .withMetrics("UnblendedCost")
                        .withNextPageToken(nextPageToken);

                GetCostAndUsageResult response = client.getCostAndUsage(request);
                for (ResultByTime item : response.getResultsByTime()) {
                    results.add(new FinanceDailyAmount(
                            LocalDate.parse(item.getTimePeriod().getStart()),
                            extractMetricAmount(item.getTotal().get("UnblendedCost"))
                    ));
                }
                nextPageToken = response.getNextPageToken();
            } while (nextPageToken != null);
        } finally {
            client.shutdown();
        }

        results.sort(Comparator.comparing(FinanceDailyAmount::date));
        return results;
    }

    private List<FinanceBreakdownAmount> fetchMonthlyBreakdown(YearMonth yearMonth) {
        validateConfigured();

        Map<String, BigDecimal> aggregated = new LinkedHashMap<>();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endExclusive = yearMonth.plusMonths(1).atDay(1);
        String nextPageToken = null;

        AWSCostExplorer client = createClient();
        try {
            do {
                GetCostAndUsageRequest request = new GetCostAndUsageRequest()
                        .withTimePeriod(new DateInterval()
                                .withStart(startDate.toString())
                                .withEnd(endExclusive.toString()))
                        .withGranularity(Granularity.MONTHLY)
                        .withMetrics("UnblendedCost")
                        .withGroupBy(new GroupDefinition()
                                .withType("DIMENSION")
                                .withKey("SERVICE"))
                        .withNextPageToken(nextPageToken);

                GetCostAndUsageResult response = client.getCostAndUsage(request);
                for (ResultByTime bucket : response.getResultsByTime()) {
                    for (Group group : bucket.getGroups()) {
                        String label = (group.getKeys() == null || group.getKeys().isEmpty())
                                ? "기타"
                                : group.getKeys().get(0);
                        MetricValue metricValue = group.getMetrics().get("UnblendedCost");
                        aggregated.merge(label, extractMetricAmount(metricValue), BigDecimal::add);
                    }
                }
                nextPageToken = response.getNextPageToken();
            } while (nextPageToken != null);
        } finally {
            client.shutdown();
        }

        return aggregated.entrySet().stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .map(entry -> new FinanceBreakdownAmount(entry.getKey(), entry.getValue()))
                .toList();
    }

    private AWSCostExplorer createClient() {
        return AWSCostExplorerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKeyId, secretAccessKey)
                ))
                .withRegion(region)
                .build();
    }

    private void validateConfigured() {
        if (!hasText(accessKeyId) || !hasText(secretAccessKey)) {
            throw new IllegalStateException("AWS 비용 조회 설정이 누락되었습니다.");
        }
    }

    private BigDecimal extractMetricAmount(MetricValue metricValue) {
        if (metricValue == null || metricValue.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(metricValue.getAmount());
    }

    private String buildDailyKey(LocalDate startDate, LocalDate endExclusive) {
        return "aws:daily:" + startDate + ":" + endExclusive;
    }

    private String buildBreakdownKey(YearMonth yearMonth) {
        return "aws:breakdown:" + yearMonth;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

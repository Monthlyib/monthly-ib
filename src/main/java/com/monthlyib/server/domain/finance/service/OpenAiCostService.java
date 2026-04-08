package com.monthlyib.server.domain.finance.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.monthlyib.server.domain.finance.model.FinanceBreakdownAmount;
import com.monthlyib.server.domain.finance.model.FinanceDailyAmount;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiCostService {

    @Value("${finance.openai.api-key:}")
    private String apiKey;

    @Value("${finance.openai.organization-id:}")
    private String organizationId;

    @Value("${finance.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Qualifier("financeRestTemplate")
    private final RestTemplate restTemplate;
    private final FinanceCacheService financeCacheService;
    private final Gson gson = new Gson();

    public ProviderLoadResult<List<FinanceDailyAmount>> getDailyCosts(LocalDate startDate, LocalDate endExclusive) {
        return financeCacheService.getOrLoad(
                buildDailyKey(startDate, endExclusive),
                "OpenAI 비용",
                () -> fetchDailyCosts(startDate, endExclusive)
        );
    }

    public ProviderLoadResult<List<FinanceBreakdownAmount>> getMonthlyBreakdown(YearMonth yearMonth) {
        return financeCacheService.getOrLoad(
                buildBreakdownKey(yearMonth),
                "OpenAI 비용 상세",
                () -> fetchMonthlyBreakdown(yearMonth)
        );
    }

    private List<FinanceDailyAmount> fetchDailyCosts(LocalDate startDate, LocalDate endExclusive) {
        validateConfigured();
        JsonArray buckets = fetchCosts(startDate, endExclusive, "1d", null);
        List<FinanceDailyAmount> results = new ArrayList<>();
        for (JsonElement bucketElement : buckets) {
            JsonObject bucket = bucketElement.getAsJsonObject();
            LocalDate date = epochSecondToDate(bucket.get("start_time"));
            results.add(new FinanceDailyAmount(date, sumBucketAmount(bucket)));
        }
        results.sort((left, right) -> left.date().compareTo(right.date()));
        return results;
    }

    private List<FinanceBreakdownAmount> fetchMonthlyBreakdown(YearMonth yearMonth) {
        validateConfigured();
        JsonArray buckets = fetchCosts(
                yearMonth.atDay(1),
                yearMonth.plusMonths(1).atDay(1),
                "1d",
                List.of("line_item")
        );

        Map<String, BigDecimal> aggregated = new LinkedHashMap<>();
        for (JsonElement bucketElement : buckets) {
            JsonObject bucket = bucketElement.getAsJsonObject();
            JsonArray results = getResults(bucket);
            for (JsonElement resultElement : results) {
                JsonObject result = resultElement.getAsJsonObject();
                String label = getString(result, "line_item");
                if (label == null || label.isBlank()) {
                    label = getString(result, "project_id");
                }
                if (label == null || label.isBlank()) {
                    label = "기타";
                }
                aggregated.merge(label, extractAmountValue(result), BigDecimal::add);
            }
        }

        return aggregated.entrySet().stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .map(entry -> new FinanceBreakdownAmount(entry.getKey(), entry.getValue()))
                .toList();
    }

    private JsonArray fetchCosts(LocalDate startDate, LocalDate endExclusive, String bucketWidth, List<String> groupBy) {
        JsonArray mergedBuckets = new JsonArray();
        String nextPage = null;
        boolean hasMore;
        do {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/organization/costs")
                    .queryParam("start_time", toEpochSecond(startDate))
                    .queryParam("end_time", toEpochSecond(endExclusive))
                    .queryParam("bucket_width", bucketWidth)
                    .queryParam("limit", 180);

            if (groupBy != null) {
                groupBy.forEach(group -> uriBuilder.queryParam("group_by", group));
            }
            if (nextPage != null) {
                uriBuilder.queryParam("page", nextPage);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (hasText(organizationId)) {
                headers.set("OpenAI-Organization", organizationId);
            }

            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        uriBuilder.build(true).toUri(),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                JsonObject body = gson.fromJson(response.getBody(), JsonObject.class);
                JsonArray data = body.has("data") && body.get("data").isJsonArray()
                        ? body.getAsJsonArray("data")
                        : new JsonArray();
                data.forEach(mergedBuckets::add);

                hasMore = body.has("has_more") && !body.get("has_more").isJsonNull() && body.get("has_more").getAsBoolean();
                nextPage = body.has("next_page") && !body.get("next_page").isJsonNull()
                        ? body.get("next_page").getAsString()
                        : null;
            } catch (RestClientException exception) {
                throw new IllegalStateException("OpenAI 비용 조회에 실패했습니다.", exception);
            }
        } while (hasMore && nextPage != null);

        return mergedBuckets;
    }

    private JsonArray getResults(JsonObject bucket) {
        return bucket.has("results") && bucket.get("results").isJsonArray()
                ? bucket.getAsJsonArray("results")
                : new JsonArray();
    }

    private BigDecimal sumBucketAmount(JsonObject bucket) {
        BigDecimal total = BigDecimal.ZERO;
        for (JsonElement resultElement : getResults(bucket)) {
            total = total.add(extractAmountValue(resultElement.getAsJsonObject()));
        }
        return total;
    }

    private BigDecimal extractAmountValue(JsonObject result) {
        if (!result.has("amount") || result.get("amount").isJsonNull()) {
            return BigDecimal.ZERO;
        }
        JsonObject amount = result.getAsJsonObject("amount");
        if (!amount.has("value") || amount.get("value").isJsonNull()) {
            return BigDecimal.ZERO;
        }
        return amount.get("value").getAsBigDecimal();
    }

    private LocalDate epochSecondToDate(JsonElement element) {
        long epochSecond = element == null || element.isJsonNull() ? 0L : element.getAsLong();
        return Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private long toEpochSecond(LocalDate date) {
        return date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    }

    private void validateConfigured() {
        if (!hasText(apiKey)) {
            throw new IllegalStateException("OpenAI 비용 조회 설정이 누락되었습니다.");
        }
    }

    private String getString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    private String buildDailyKey(LocalDate startDate, LocalDate endExclusive) {
        return "openai:daily:" + startDate + ":" + endExclusive;
    }

    private String buildBreakdownKey(YearMonth yearMonth) {
        return "openai:breakdown:" + yearMonth;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

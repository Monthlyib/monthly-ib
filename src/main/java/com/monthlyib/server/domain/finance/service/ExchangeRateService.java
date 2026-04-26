package com.monthlyib.server.domain.finance.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.monthlyib.server.domain.finance.model.ExchangeRateRange;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import com.monthlyib.server.domain.finance.repository.AdminFinanceDailySnapshotJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    @Value("${finance.exchange.base-url:https://api.frankfurter.app}")
    private String baseUrl;

    @Value("${finance.exchange.fallback-usd-krw-rate:1400}")
    private BigDecimal fallbackUsdKrwRate;

    private final RestTemplate restTemplate;
    private final FinanceCacheService financeCacheService;
    private final AdminFinanceDailySnapshotJpaRepository snapshotRepository;
    private final Gson gson = new Gson();

    public ProviderLoadResult<ExchangeRateRange> getUsdKrwRates(LocalDate startDate, LocalDate endDate) {
        ProviderLoadResult<ExchangeRateRange> result = financeCacheService.getOrLoad(
                buildKey(startDate, endDate),
                "환율",
                () -> fetchRange(startDate, endDate)
        );
        if (result.available()) {
            return result;
        }
        return fallbackRange(startDate, endDate, result.warningMessage());
    }

    public ProviderLoadResult<ExchangeRateRange> loadUsdKrwRates(LocalDate startDate, LocalDate endDate) {
        try {
            return ProviderLoadResult.success(
                    fetchRange(startDate, endDate),
                    LocalDateTime.now(),
                    false,
                    null
            );
        } catch (Exception exception) {
            log.warn("Failed to load USD/KRW exchange rates. Falling back to stored/default rate.", exception);
            return fallbackRange(startDate, endDate, "환율 API를 불러오지 못해 대체 환율을 사용했습니다.");
        }
    }

    private ExchangeRateRange fetchRange(LocalDate startDate, LocalDate endDate) {
        String path = startDate.equals(endDate)
                ? "/" + startDate
                : "/" + startDate + ".." + endDate;

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                            .queryParam("from", "USD")
                            .queryParam("to", "KRW")
                            .build(true)
                            .toUri(),
                    HttpMethod.GET,
                    null,
                    String.class
            );

            JsonObject body = gson.fromJson(response.getBody(), JsonObject.class);
            Map<LocalDate, BigDecimal> rates = new LinkedHashMap<>();
            if (body.has("rates") && body.get("rates").isJsonObject()) {
                JsonObject rawRates = body.getAsJsonObject("rates");
                parseRates(body, rawRates, startDate, rates);
            }

            if (rates.isEmpty()) {
                throw new IllegalStateException("USD/KRW 환율 응답에 KRW 값이 없습니다.");
            }

            LocalDate latestRateDate = rates.keySet().stream().max(LocalDate::compareTo).orElse(null);
            return new ExchangeRateRange(rates, latestRateDate);
        } catch (RestClientException exception) {
            throw new IllegalStateException("환율 데이터를 불러오지 못했습니다.", exception);
        }
    }

    private void parseRates(JsonObject body,
                            JsonObject rawRates,
                            LocalDate startDate,
                            Map<LocalDate, BigDecimal> rates) {
        // Single-day response: {"date":"2026-04-24","rates":{"KRW":1477.04}}
        if (rawRates.has("KRW") && !rawRates.get("KRW").isJsonNull()) {
            LocalDate rateDate = body.has("date") && !body.get("date").isJsonNull()
                    ? LocalDate.parse(body.get("date").getAsString())
                    : startDate;
            rates.put(rateDate, rawRates.get("KRW").getAsBigDecimal());
            return;
        }

        // Range response: {"rates":{"2026-04-24":{"KRW":1477.04}}}
        for (Map.Entry<String, JsonElement> entry : rawRates.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            JsonObject currencyMap = entry.getValue().getAsJsonObject();
            if (currencyMap.has("KRW") && !currencyMap.get("KRW").isJsonNull()) {
                rates.put(LocalDate.parse(entry.getKey()), currencyMap.get("KRW").getAsBigDecimal());
            }
        }
    }

    private ProviderLoadResult<ExchangeRateRange> fallbackRange(LocalDate startDate, LocalDate endDate, String warningMessage) {
        BigDecimal fallbackRate = snapshotRepository.findTopByUsdKrwRateIsNotNullOrderBySnapshotDateDesc()
                .map(snapshot -> Optional.ofNullable(snapshot.getUsdKrwRate()).orElse(fallbackUsdKrwRate))
                .orElse(fallbackUsdKrwRate);

        Map<LocalDate, BigDecimal> rates = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            rates.put(cursor, fallbackRate);
            cursor = cursor.plusDays(1);
        }

        String resolvedWarning = Optional.ofNullable(warningMessage)
                .filter(message -> !message.isBlank())
                .orElse("환율 API를 불러오지 못해 대체 환율을 사용했습니다.");
        return ProviderLoadResult.success(
                new ExchangeRateRange(rates, endDate),
                LocalDateTime.now(),
                true,
                resolvedWarning
        );
    }

    private String buildKey(LocalDate startDate, LocalDate endDate) {
        return "exchange:usd-krw:" + startDate + ":" + endDate;
    }
}

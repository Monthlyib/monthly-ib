package com.monthlyib.server.domain.finance.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.monthlyib.server.domain.finance.model.ExchangeRateRange;
import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    @Value("${finance.exchange.base-url:https://api.frankfurter.app}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final FinanceCacheService financeCacheService;
    private final Gson gson = new Gson();

    public ProviderLoadResult<ExchangeRateRange> getUsdKrwRates(LocalDate startDate, LocalDate endDate) {
        return financeCacheService.getOrLoad(
                buildKey(startDate, endDate),
                "환율",
                () -> fetchRange(startDate, endDate)
        );
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
                for (Map.Entry<String, JsonElement> entry : rawRates.entrySet()) {
                    JsonObject currencyMap = entry.getValue().getAsJsonObject();
                    if (currencyMap.has("KRW") && !currencyMap.get("KRW").isJsonNull()) {
                        rates.put(LocalDate.parse(entry.getKey()), currencyMap.get("KRW").getAsBigDecimal());
                    }
                }
            }

            LocalDate latestRateDate = rates.keySet().stream().max(LocalDate::compareTo).orElse(null);
            return new ExchangeRateRange(rates, latestRateDate);
        } catch (RestClientException exception) {
            throw new IllegalStateException("환율 데이터를 불러오지 못했습니다.", exception);
        }
    }

    private String buildKey(LocalDate startDate, LocalDate endDate) {
        return "exchange:usd-krw:" + startDate + ":" + endDate;
    }
}

package com.monthlyib.server.domain.finance.service;

import com.monthlyib.server.domain.finance.model.ProviderLoadResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class FinanceCacheService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public <T> ProviderLoadResult<T> getOrLoad(String key, String label, Supplier<T> loader) {
        CacheEntry cached = cache.get(key);
        if (cached != null && !isExpired(cached.fetchedAt())) {
            return ProviderLoadResult.success(cast(cached.data()), cached.fetchedAt(), false, null);
        }

        try {
            T loaded = loader.get();
            LocalDateTime fetchedAt = LocalDateTime.now();
            cache.put(key, new CacheEntry(loaded, fetchedAt));
            return ProviderLoadResult.success(loaded, fetchedAt, false, null);
        } catch (Exception exception) {
            if (cached != null) {
                return ProviderLoadResult.success(
                        cast(cached.data()),
                        cached.fetchedAt(),
                        true,
                        label + " 최근 캐시 데이터를 사용합니다."
                );
            }
            return ProviderLoadResult.failure(label + " 데이터를 불러오지 못했습니다.");
        }
    }

    private boolean isExpired(LocalDateTime fetchedAt) {
        return fetchedAt == null || fetchedAt.atZone(ZoneId.systemDefault()).toInstant()
                .isBefore(Instant.now().minus(TTL));
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object data) {
        return (T) data;
    }

    private record CacheEntry(Object data, LocalDateTime fetchedAt) {
    }
}

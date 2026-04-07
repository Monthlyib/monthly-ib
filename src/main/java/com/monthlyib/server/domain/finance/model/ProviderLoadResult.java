package com.monthlyib.server.domain.finance.model;

import java.time.LocalDateTime;

public record ProviderLoadResult<T>(
        T data,
        LocalDateTime fetchedAt,
        boolean stale,
        boolean available,
        String warningMessage
) {

    public static <T> ProviderLoadResult<T> success(T data, LocalDateTime fetchedAt, boolean stale, String warningMessage) {
        return new ProviderLoadResult<>(data, fetchedAt, stale, true, warningMessage);
    }

    public static <T> ProviderLoadResult<T> failure(String warningMessage) {
        return new ProviderLoadResult<>(null, null, false, false, warningMessage);
    }
}

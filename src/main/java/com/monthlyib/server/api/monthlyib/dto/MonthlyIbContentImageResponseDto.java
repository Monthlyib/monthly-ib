package com.monthlyib.server.api.monthlyib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyIbContentImageResponseDto {

    private String imageUrl;

    public static MonthlyIbContentImageResponseDto of(String imageUrl) {
        return MonthlyIbContentImageResponseDto.builder()
                .imageUrl(imageUrl)
                .build();
    }
}

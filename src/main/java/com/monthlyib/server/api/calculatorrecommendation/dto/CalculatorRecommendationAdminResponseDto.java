package com.monthlyib.server.api.calculatorrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorRecommendationAdminResponseDto {

    private String pageKey;

    private CalculatorRecommendationConfigDto config;

    private Long updatedBy;

    private LocalDateTime updatedAt;
}

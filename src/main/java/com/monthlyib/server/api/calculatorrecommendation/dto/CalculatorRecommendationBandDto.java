package com.monthlyib.server.api.calculatorrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorRecommendationBandDto {

    private String key;

    private String label;

    private Integer minScore;
}

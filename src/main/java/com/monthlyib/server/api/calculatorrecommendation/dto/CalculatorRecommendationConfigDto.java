package com.monthlyib.server.api.calculatorrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorRecommendationConfigDto {

    private List<CalculatorRecommendationBandDto> scoreBands;

    private List<CalculatorRecommendationCountryDto> countries;
}

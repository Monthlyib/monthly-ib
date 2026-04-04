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
public class CalculatorRecommendationCountryDto {

    private String code;

    private String label;

    private List<CalculatorRecommendationSchoolDto> schools;
}

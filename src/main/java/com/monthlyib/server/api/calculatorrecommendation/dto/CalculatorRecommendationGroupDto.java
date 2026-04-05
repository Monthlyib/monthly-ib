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
public class CalculatorRecommendationGroupDto {

    private String key;

    private String label;

    private Integer maxSelectableCount;

    private List<CalculatorRecommendationGroupSubjectDto> subjects;
}

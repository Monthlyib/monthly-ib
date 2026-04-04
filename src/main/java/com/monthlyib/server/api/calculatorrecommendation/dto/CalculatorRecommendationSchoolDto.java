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
public class CalculatorRecommendationSchoolDto {

    private String id;

    private String name;

    private String img;

    private String ibScore;

    private String rank;

    private String tuition;

    private List<String> bandKeys;
}

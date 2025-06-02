package com.monthlyib.server.api.aidescriptive.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GptFeedbackResult {
    private Integer score;
    private Integer maxScore;
    private String studentAnswer;
    private String modelAnswer;
    private String improvementKo;
    private String improvementEn;
}

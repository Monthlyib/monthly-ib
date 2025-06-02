package com.monthlyib.server.api.aidescriptive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveResultDto {
    private Long questionId;
    private String subject;
    private String chapter;
    private String question;
    private String imagePath;

    private Long answerId;
    private String answerText;
    private Integer score;
    private Integer maxScore;

    private String feedbackEnglish;
    private String feedbackKorean;
    private String modelAnswer;
}

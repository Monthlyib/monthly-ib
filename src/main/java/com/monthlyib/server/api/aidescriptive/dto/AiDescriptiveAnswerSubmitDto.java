package com.monthlyib.server.api.aidescriptive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveAnswerSubmitDto {

    private String subject;
    private String chapter;
    private Long questionId;
    private String answer;
}

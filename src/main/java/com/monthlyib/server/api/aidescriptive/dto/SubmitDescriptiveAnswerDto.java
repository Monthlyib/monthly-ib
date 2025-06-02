package com.monthlyib.server.api.aidescriptive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDescriptiveAnswerDto {
    private String subject;
    private String chapter;
    private Long questionId;
    private String answer;
}

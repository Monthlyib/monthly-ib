package com.monthlyib.server.domain.aiio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationIssue {

    private String word;
    private Double accuracyScore;
    private String errorType;
}

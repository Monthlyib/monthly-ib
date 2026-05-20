package com.monthlyib.server.domain.aiio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzurePronunciationAssessmentResult {

    private String transcript;
    private String rawJson;
    private SpeechMetrics metrics;
    private List<PronunciationIssue> issues;
}

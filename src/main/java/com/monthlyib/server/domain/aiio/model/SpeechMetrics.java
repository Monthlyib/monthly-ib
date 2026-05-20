package com.monthlyib.server.domain.aiio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechMetrics {

    private Double pronunciationScore;
    private Double accuracyScore;
    private Double fluencyScore;
    private Double completenessScore;
    private Double prosodyScore;
    private Double speakingRateWpm;
    private Integer durationSeconds;
}

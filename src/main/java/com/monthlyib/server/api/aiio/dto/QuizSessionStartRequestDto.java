package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSessionStartRequestDto {
    private String subject;
    private String chapter;
    private int questionCount;
    private int durationMinutes;
}

package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponseDto {
    private Long quizSessionId;
    private String subject;
    private String chapter;
    private int totalQuestions;
    private int correctAnswers;
    private int totalTimeSeconds;
    private LocalDateTime submittedAt;
}

package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<QuestionDetail> questionDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDetail {
        private Long questionId;
        private String question;
        private String choiceA;
        private String choiceB;
        private String choiceC;
        private String choiceD;
        private String answer;
        private String subject;
        private String chapter;
        private String userAnswer;
        private boolean correct;
        private int elapsedTime;
    }
}

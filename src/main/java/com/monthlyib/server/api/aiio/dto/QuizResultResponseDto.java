package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponseDto {

    private Long sessionId;
    private int totalQuestions;
    private int correctCount;
    private double score;
    private List<QuizSessionQuestionResponseDto> questions;

    public static QuizResultResponseDto of(QuizSession session, List<QuizSessionQuestion> questions) {
        QuizResultResponseDto dto = new QuizResultResponseDto();
        dto.setSessionId(session.getId());
        dto.setTotalQuestions(questions.size());

        int correct = (int) questions.stream().filter(QuizSessionQuestion::isCorrect).count();
        dto.setCorrectCount(correct);
        dto.setScore(questions.isEmpty() ? 0.0 : (double) correct / questions.size() * 100.0);

        List<QuizSessionQuestionResponseDto> questionDtos = questions.stream()
                .map(QuizSessionQuestionResponseDto::of)
                .collect(Collectors.toList());
        dto.setQuestions(questionDtos);

        return dto;
    }
}

package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.entity.QuizSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSessionStartResponseDto {

    private Long quizSessionId;
    private String subject;
    private String chapter;
    private LocalDateTime startedAt;
    private int durationMinutes;
    private List<QuestionSummaryDto> questions;

    public static QuizSessionStartResponseDto of(QuizSession session, List<AiChapterTest> selectedQuestions) {
        return QuizSessionStartResponseDto.builder()
                .quizSessionId(session.getId())
                .subject(session.getSubject())
                .chapter(session.getChapter())
                .startedAt(session.getStartedAt())
                .durationMinutes(session.getDurationMinutes())
                .questions(selectedQuestions.stream()
                        .map(q -> new QuestionSummaryDto(q.getId(), q.getQuestion()))
                        .collect(Collectors.toList()))
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class QuestionSummaryDto {
        private Long id;
        private String question;
    }
}

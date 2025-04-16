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
    private List<FullQuestionInfoDto> questions;

    public static QuizSessionStartResponseDto of(QuizSession session, List<AiChapterTest> selectedQuestions) {
        return QuizSessionStartResponseDto.builder()
                .quizSessionId(session.getId())
                .subject(session.getSubject())
                .chapter(session.getChapter())
                .startedAt(session.getStartedAt())
                .durationMinutes(session.getDurationMinutes())
                .questions(selectedQuestions.stream()
                        .map(q -> new FullQuestionInfoDto(
                                q.getId(),
                                q.getQuestion(),
                                q.getChoiceA(),
                                q.getChoiceB(),
                                q.getChoiceC(),
                                q.getChoiceD(),
                                q.getAnswer(),
                                q.getSubject(),
                                q.getChapter()
                        ))
                        .collect(Collectors.toList()))
                .build();
    }

    public static QuizSessionStartResponseDto ofWithAllFields(QuizSession session, List<AiChapterTest> selectedQuestions) {
        List<FullQuestionInfoDto> fullDetails = selectedQuestions.stream().map(q ->
            new FullQuestionInfoDto(
                q.getId(),
                q.getQuestion(),
                q.getChoiceA(),
                q.getChoiceB(),
                q.getChoiceC(),
                q.getChoiceD(),
                q.getAnswer(),
                q.getSubject(),
                q.getChapter()
            )
        ).collect(Collectors.toList());

        return QuizSessionStartResponseDto.builder()
                .quizSessionId(session.getId())
                .subject(session.getSubject())
                .chapter(session.getChapter())
                .startedAt(session.getStartedAt())
                .durationMinutes(session.getDurationMinutes())
                .questions(fullDetails)
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FullQuestionInfoDto {
        private Long id;
        private String question;
        private String choiceA;
        private String choiceB;
        private String choiceC;
        private String choiceD;
        private String answer;
        private String subject;
        private String chapter;
    }
}

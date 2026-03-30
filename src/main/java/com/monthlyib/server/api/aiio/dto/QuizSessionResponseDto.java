package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionResponseDto {

    private Long sessionId;
    private String subject;
    private String chapter;
    private boolean isSubmitted;
    private LocalDateTime startedAt;
    private Integer durationMinutes;
    private List<QuizSessionQuestionResponseDto> questions;

    public static QuizSessionResponseDto of(QuizSession session, List<QuizSessionQuestionResponseDto> questions) {
        QuizSessionResponseDto dto = new QuizSessionResponseDto();
        dto.setSessionId(session.getId());
        dto.setSubject(session.getSubject());
        dto.setChapter(session.getChapter());
        dto.setSubmitted(session.isSubmitted());
        dto.setStartedAt(session.getStartedAt());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setQuestions(questions);
        return dto;
    }
}

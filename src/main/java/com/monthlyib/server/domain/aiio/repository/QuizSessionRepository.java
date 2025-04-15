package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import java.util.List;

public interface QuizSessionRepository {
    QuizSession save(QuizSession session);
    QuizSession findById(Long id);
    List<QuizSession> findAllByUserIdAndIsSubmittedFalse(Long userId);
    void delete(QuizSession session);
}

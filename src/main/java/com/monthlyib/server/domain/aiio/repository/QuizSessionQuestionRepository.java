package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;

import java.util.List;

public interface QuizSessionQuestionRepository {
    QuizSessionQuestion save(QuizSessionQuestion question);
    List<QuizSessionQuestion> findByQuizSessionId(Long quizSessionId);
    void delete(QuizSessionQuestion question);
}

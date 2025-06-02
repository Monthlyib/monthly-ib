package com.monthlyib.server.domain.aiio.repository;

import java.util.List;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;

public interface QuizSessionQuestionRepository {
    QuizSessionQuestion save(QuizSessionQuestion question);
    List<QuizSessionQuestion> findByQuizSessionId(Long quizSessionId);
    void delete(QuizSessionQuestion question);
    List<QuizSessionQuestion> findByQuizSessionIdWithChapterTest(Long quizSessionId);
    List<QuizSessionQuestion> findWithChapterTestByQuizSessionId(Long quizSessionId); // Added method for fetch join
}

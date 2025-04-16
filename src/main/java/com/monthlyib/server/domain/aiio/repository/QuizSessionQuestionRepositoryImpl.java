package com.monthlyib.server.domain.aiio.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QuizSessionQuestionRepositoryImpl implements QuizSessionQuestionRepository {

    private final QuizSessionQuestionJpaRepository jpaRepository;

    @Override
    public QuizSessionQuestion save(QuizSessionQuestion question) {
        return jpaRepository.save(question);
    }

    @Override
    public List<QuizSessionQuestion> findByQuizSessionId(Long quizSessionId) {
        return jpaRepository.findAll().stream()
                .filter(q -> q.getQuizSession().getId().equals(quizSessionId))
                .toList();
    }

    @Override
    public void delete(QuizSessionQuestion question) {
        jpaRepository.delete(question);
    }

    @Override
    public List<QuizSessionQuestion> findByQuizSessionIdWithChapterTest(Long quizSessionId) {
        return jpaRepository.findByQuizSessionIdWithChapterTest(quizSessionId);
    }

    @Override
    public List<QuizSessionQuestion> findWithChapterTestByQuizSessionId(Long quizSessionId) {
        return jpaRepository.findWithChapterTestByQuizSessionId(quizSessionId);
    }
}

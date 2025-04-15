package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}

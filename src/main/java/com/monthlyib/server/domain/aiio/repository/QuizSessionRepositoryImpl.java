package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuizSessionRepositoryImpl implements QuizSessionRepository {

    private final QuizSessionJpaRepository jpaRepository;

    @Override
    public QuizSession save(QuizSession session) {
        return jpaRepository.save(session);
    }

    @Override
    public QuizSession findById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("QuizSession not found with id: " + id));
    }

    @Override
    public List<QuizSession> findAllByUserIdAndIsSubmittedFalse(Long userId) {
        return jpaRepository.findAll().stream()
                .filter(q -> q.getUser().getUserId().equals(userId) && !q.isSubmitted())
                .toList();
    }

    @Override
    public void delete(QuizSession session) {
        jpaRepository.delete(session);
    }
}

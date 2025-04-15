package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSessionQuestionJpaRepository extends JpaRepository<QuizSessionQuestion, Long> {
}

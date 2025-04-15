package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSessionJpaRepository extends JpaRepository<QuizSession, Long> {
}

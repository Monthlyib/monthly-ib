package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizSessionJpaRepository extends JpaRepository<QuizSession, Long> {

    Optional<QuizSession> findByUserUserIdAndSubjectAndChapterAndIsSubmittedFalse(
            Long userId, String subject, String chapter);

    List<QuizSession> findAllByUserUserId(Long userId);
}

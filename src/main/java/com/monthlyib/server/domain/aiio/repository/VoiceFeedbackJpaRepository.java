package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoiceFeedbackJpaRepository extends JpaRepository<VoiceFeedback, Long> {
    Optional<VoiceFeedback> findByAuthorId(Long authorId);
}
package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoiceFeedbackJpaRepository extends JpaRepository<VoiceFeedback, Long> {

    List<VoiceFeedback> findAllByAuthorId(Long authorId);
}

package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDescriptiveAnswerJpaRepository extends JpaRepository<AiDescriptiveAnswer, Long> {

    List<AiDescriptiveAnswer> findAllByUserUserId(Long userId);
}

package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiDescriptiveAnswerJpaRepository extends JpaRepository<AiDescriptiveAnswer, Long> {
}

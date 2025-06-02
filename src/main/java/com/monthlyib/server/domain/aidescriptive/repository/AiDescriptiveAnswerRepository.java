package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.user.entity.User;

import java.util.Optional;

public interface AiDescriptiveAnswerRepository {
    AiDescriptiveAnswer save(AiDescriptiveAnswer answer);
    Optional<AiDescriptiveAnswer> findByUserAndDescriptiveQuestionId(User user, Long descriptiveQuestionId);
    Optional<AiDescriptiveAnswer> findById(Long id);
}
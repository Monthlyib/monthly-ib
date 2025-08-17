package com.monthlyib.server.domain.aiia.repository;

import java.util.Optional;

import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;

public interface AiIARecommendationRepository {
    AiIARecommendation save(AiIARecommendation recommendation);
    Optional<AiIARecommendation> findById(Long id);
}

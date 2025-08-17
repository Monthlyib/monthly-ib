package com.monthlyib.server.domain.aiia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;

@Repository
public interface AiIARecommendationJpaRepository extends JpaRepository<AiIARecommendation, Long> {
}

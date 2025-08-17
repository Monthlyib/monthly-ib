package com.monthlyib.server.domain.aiia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;

@Repository
public class AiIARecommendationRepositoryImpl extends QuerydslRepositorySupport implements AiIARecommendationRepository {

    private final AiIARecommendationJpaRepository jpaRepository;

    public AiIARecommendationRepositoryImpl(AiIARecommendationJpaRepository jpaRepository) {
        super(AiIARecommendation.class);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AiIARecommendation save(AiIARecommendation recommendation) {
        return jpaRepository.save(recommendation);
    }

    @Override
    public Optional<AiIARecommendation> findById(Long id) {
        return jpaRepository.findById(id);
    }

}

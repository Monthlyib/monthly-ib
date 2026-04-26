package com.monthlyib.server.domain.aiia.repository;

import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiIARecommendationJpaRepository extends JpaRepository<AiIARecommendation, Long> {

    Optional<AiIARecommendation> findByUserUserIdAndSubjectAndInterest(Long userId, String subject, String interest);

    List<AiIARecommendation> findAllByUserUserId(Long userId);
}

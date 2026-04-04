package com.monthlyib.server.domain.calculatorrecommendation.repository;

import com.monthlyib.server.domain.calculatorrecommendation.entity.CalculatorRecommendationPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CalculatorRecommendationPageJpaRepository extends JpaRepository<CalculatorRecommendationPage, Long> {

    Optional<CalculatorRecommendationPage> findByPageKey(String pageKey);
}

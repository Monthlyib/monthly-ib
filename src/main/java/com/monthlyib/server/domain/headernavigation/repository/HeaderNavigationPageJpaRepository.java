package com.monthlyib.server.domain.headernavigation.repository;

import com.monthlyib.server.domain.headernavigation.entity.HeaderNavigationPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeaderNavigationPageJpaRepository extends JpaRepository<HeaderNavigationPage, Long> {

    Optional<HeaderNavigationPage> findByPageKey(String pageKey);
}

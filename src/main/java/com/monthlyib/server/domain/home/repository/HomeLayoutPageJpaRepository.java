package com.monthlyib.server.domain.home.repository;

import com.monthlyib.server.domain.home.entity.HomeLayoutPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeLayoutPageJpaRepository extends JpaRepository<HomeLayoutPage, Long> {

    Optional<HomeLayoutPage> findByPageKey(String pageKey);
}

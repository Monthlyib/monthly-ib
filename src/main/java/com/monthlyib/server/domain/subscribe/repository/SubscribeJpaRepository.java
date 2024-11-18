package com.monthlyib.server.domain.subscribe.repository;

import com.monthlyib.server.domain.subscribe.entity.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeJpaRepository extends JpaRepository<Subscribe, Long> {
}

package com.monthlyib.server.domain.montlyib.repository;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyIbJpaRepository extends JpaRepository<MonthlyIb, Long> {
}

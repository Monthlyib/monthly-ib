package com.monthlyib.server.domain.finance.repository;

import com.monthlyib.server.domain.finance.entity.AdminFinanceDailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdminFinanceDailySnapshotJpaRepository extends JpaRepository<AdminFinanceDailySnapshot, LocalDate> {

    List<AdminFinanceDailySnapshot> findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);

    Optional<AdminFinanceDailySnapshot> findTopByUsdKrwRateIsNotNullOrderBySnapshotDateDesc();
}

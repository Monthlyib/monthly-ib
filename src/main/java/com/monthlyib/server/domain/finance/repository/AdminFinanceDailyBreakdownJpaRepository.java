package com.monthlyib.server.domain.finance.repository;

import com.monthlyib.server.domain.finance.entity.AdminFinanceDailyBreakdown;
import com.monthlyib.server.domain.finance.entity.FinanceProvider;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdminFinanceDailyBreakdownJpaRepository extends JpaRepository<AdminFinanceDailyBreakdown, Long> {

    List<AdminFinanceDailyBreakdown> findAllBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);

    @Transactional
    void deleteAllBySnapshotDateAndProvider(LocalDate snapshotDate, FinanceProvider provider);
}

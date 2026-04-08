package com.monthlyib.server.domain.finance.repository;

import com.monthlyib.server.domain.finance.entity.AdminFinanceSyncJob;
import com.monthlyib.server.domain.finance.entity.FinanceJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminFinanceSyncJobJpaRepository extends JpaRepository<AdminFinanceSyncJob, Long> {

    Optional<AdminFinanceSyncJob> findTopByJobStatusOrderByStartedAtDesc(FinanceJobStatus jobStatus);

    Optional<AdminFinanceSyncJob> findTopByFinishedAtIsNotNullOrderByFinishedAtDesc();
}

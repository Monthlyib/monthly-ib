package com.monthlyib.server.domain.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "admin_finance_sync_job",
        indexes = {
                @Index(name = "idx_finance_sync_job_status", columnList = "jobStatus"),
                @Index(name = "idx_finance_sync_job_started", columnList = "startedAt")
        }
)
public class AdminFinanceSyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminFinanceSyncJobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceSyncTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceJobStatus jobStatus;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(length = 1000)
    private String warningMessage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}

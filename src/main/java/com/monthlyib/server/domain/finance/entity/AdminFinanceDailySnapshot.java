package com.monthlyib.server.domain.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admin_finance_daily_snapshot")
public class AdminFinanceDailySnapshot {

    @Id
    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(precision = 19, scale = 6)
    private BigDecimal awsCostUsd;

    @Column(precision = 19, scale = 2)
    private BigDecimal awsCostKrw;

    @Column(precision = 19, scale = 6)
    private BigDecimal openAiCostUsd;

    @Column(precision = 19, scale = 2)
    private BigDecimal openAiCostKrw;

    @Column(precision = 19, scale = 2)
    private BigDecimal revenueKrw;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalOperatingCostKrw;

    @Column(precision = 19, scale = 2)
    private BigDecimal operatingProfitKrw;

    @Column(precision = 19, scale = 4)
    private BigDecimal usdKrwRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceSyncStatus awsStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceSyncStatus openAiStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceSyncStatus revenueStatus;

    @Column(length = 1000)
    private String warningMessage;

    @Column(nullable = false)
    private LocalDateTime syncedAt;
}

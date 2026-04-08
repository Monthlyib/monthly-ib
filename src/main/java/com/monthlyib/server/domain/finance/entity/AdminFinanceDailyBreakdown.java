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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "admin_finance_daily_breakdown",
        indexes = {
                @Index(name = "idx_finance_breakdown_snapshot_date", columnList = "snapshotDate"),
                @Index(name = "idx_finance_breakdown_provider", columnList = "provider")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_finance_breakdown_date_provider_key",
                        columnNames = {"snapshotDate", "provider", "breakdownKey"}
                )
        }
)
public class AdminFinanceDailyBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminFinanceDailyBreakdownId;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinanceProvider provider;

    @Column(nullable = false, length = 255)
    private String breakdownKey;

    @Column(precision = 19, scale = 6)
    private BigDecimal amountUsd;

    @Column(precision = 19, scale = 2)
    private BigDecimal amountKrw;
}

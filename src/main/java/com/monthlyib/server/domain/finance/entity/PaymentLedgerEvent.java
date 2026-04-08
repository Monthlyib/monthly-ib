package com.monthlyib.server.domain.finance.entity;

import com.monthlyib.server.audit.Auditable;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "payment_ledger_event",
        indexes = {
                @Index(name = "idx_payment_ledger_event_date", columnList = "eventDate"),
                @Index(name = "idx_payment_ledger_event_source_ref", columnList = "sourceRefId"),
                @Index(name = "idx_payment_ledger_event_payment_key", columnList = "externalPaymentKey")
        }
)
public class PaymentLedgerEvent extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentLedgerEventId;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentLedgerEventType eventType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountKrw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentLedgerSourceType sourceType;

    private Long sourceRefId;

    @Column(length = 255)
    private String externalPaymentKey;

    @Column(length = 500)
    private String memo;
}

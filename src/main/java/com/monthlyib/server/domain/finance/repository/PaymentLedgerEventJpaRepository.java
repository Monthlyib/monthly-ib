package com.monthlyib.server.domain.finance.repository;

import com.monthlyib.server.domain.finance.entity.PaymentLedgerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentLedgerEventJpaRepository extends JpaRepository<PaymentLedgerEvent, Long> {

    List<PaymentLedgerEvent> findAllByEventDateBetweenOrderByEventDateAsc(LocalDate startDate, LocalDate endDate);
}

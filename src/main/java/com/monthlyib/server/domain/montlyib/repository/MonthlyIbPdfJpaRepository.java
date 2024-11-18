package com.monthlyib.server.domain.montlyib.repository;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIbPdfFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonthlyIbPdfJpaRepository extends JpaRepository<MonthlyIbPdfFile, Long> {

    List<MonthlyIbPdfFile> findAllByMonthlyIbId(Long monthlyIbId);

    void deleteAllByMonthlyIbId(Long monthlyIbId);

}

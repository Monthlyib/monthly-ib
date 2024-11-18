package com.monthlyib.server.domain.montlyib.repository;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIbThumbnailFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyIbThumbnailJpaRepository extends JpaRepository<MonthlyIbThumbnailFile, Long> {

    Optional<MonthlyIbThumbnailFile> findByMonthlyIbId(Long monthlyIbId);

    void deleteByMonthlyIbId(Long monthlyIbId);
}

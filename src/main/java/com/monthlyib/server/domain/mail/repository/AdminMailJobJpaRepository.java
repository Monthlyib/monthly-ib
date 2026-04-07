package com.monthlyib.server.domain.mail.repository;

import com.monthlyib.server.domain.mail.entity.AdminMailJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminMailJobJpaRepository extends JpaRepository<AdminMailJob, Long> {

    List<AdminMailJob> findTop20ByOrderByCreateAtDesc();
}

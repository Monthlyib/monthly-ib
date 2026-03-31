package com.monthlyib.server.domain.tutoring.repository;

import com.monthlyib.server.domain.tutoring.entity.TutoringEmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutoringEmailTemplateJpaRepository extends JpaRepository<TutoringEmailTemplate, Long> {
    Optional<TutoringEmailTemplate> findFirstByActiveTrue();
}

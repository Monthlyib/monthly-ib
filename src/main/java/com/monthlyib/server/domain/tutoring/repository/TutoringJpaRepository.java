package com.monthlyib.server.domain.tutoring.repository;

import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TutoringJpaRepository extends JpaRepository<Tutoring, Long> {

    List<Tutoring> findAllByDate(LocalDate date);
    List<Tutoring> findAllByDateAndHourAndMinute(LocalDate date, int hour, int minute);
}

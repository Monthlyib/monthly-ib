package com.monthlyib.server.domain.tutoring.repository;

import com.monthlyib.server.api.tutoring.dto.TutoringResponseDto;
import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TutoringRepository {

    List<Tutoring> findAllByDate(LocalDate date, int hour, int minute);

    List<TutoringResponseDto> findAllByDate(LocalDate date);


    Page<TutoringResponseDto> findAllDtoByDate(LocalDate date, TutoringStatus status, Long userId, Pageable pageable);

    Optional<Tutoring> findByTutoringId(Long tutoringId);

    Tutoring save(Tutoring tutoring);

    void delete(Long tutoringId);
}

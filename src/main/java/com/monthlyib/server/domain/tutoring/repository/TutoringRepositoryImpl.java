package com.monthlyib.server.domain.tutoring.repository;

import com.monthlyib.server.api.tutoring.dto.TutoringResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.tutoring.entity.QTutoring;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TutoringRepositoryImpl extends QuerydslRepositorySupport implements TutoringRepository {

    private final TutoringJpaRepository tutoringJpaRepository;

    public TutoringRepositoryImpl(TutoringJpaRepository tutoringJpaRepository) {
        super(Tutoring.class);
        this.tutoringJpaRepository = tutoringJpaRepository;
    }

    QTutoring tutoring = QTutoring.tutoring;

    @Override
    public List<Tutoring> findAllByDate(LocalDate date, int hour, int minute) {
        return tutoringJpaRepository.findAllByDateAndHourAndMinute(date, hour, minute);
    }

    @Override
    public List<TutoringResponseDto> findAllByDate(LocalDate date) {
        return List.of();
    }

    @Override
    public Page<TutoringResponseDto> findAllDtoByDate(LocalDate date, TutoringStatus status, Long userId, Pageable pageable) {
        JPQLQuery<TutoringResponseDto> query = getTutoringResponseQuery();
        if (date != null) {
            query.where(tutoring.date.eq(date));
        }
        if (status != null) {
            query.where(tutoring.tutoringStatus.eq(status));
        }
        if (userId != null) {
            query.where(tutoring.requestUserId.eq(userId));
        }
        List<TutoringResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public Optional<Tutoring> findByTutoringId(Long tutoringId) {
        return tutoringJpaRepository.findById(tutoringId);
    }

    @Override
    public Tutoring save(Tutoring tutoring) {
        return tutoringJpaRepository.save(tutoring);
    }

    @Override
    public void delete(Long tutoringId) {
        tutoringJpaRepository.deleteById(tutoringId);
    }

    private JPQLQuery<TutoringResponseDto> getTutoringResponseQuery() {
        return from(tutoring)
                .select(
                        Projections.constructor(
                                TutoringResponseDto.class,
                                tutoring.tutoringId,
                                tutoring.date,
                                tutoring.hour,
                                tutoring.minute,
                                tutoring.requestUserId,
                                tutoring.requestUsername,
                                tutoring.requestUserNickName,
                                tutoring.detail,
                                tutoring.tutoringStatus,
                                tutoring.createAt,
                                tutoring.updateAt
                        )
                );
    }
}

package com.monthlyib.server.domain.montlyib.repository;

import com.monthlyib.server.api.monthlyib.dto.MonthlyIbPdfFileResponseDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbResponseDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbSearchDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbSimpleResponseDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbPdfFile;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbThumbnailFile;
import com.monthlyib.server.domain.montlyib.entity.QMonthlyIb;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MonthlyIbRepositoryImpl extends QuerydslRepositorySupport implements MonthlyIbRepository {

    private final MonthlyIbJpaRepository monthlyIbJpaRepository;

    private final MonthlyIbPdfJpaRepository monthlyIbPdfJpaRepository;

    private final MonthlyIbThumbnailJpaRepository monthlyIbThumbnailJpaRepository;

    QMonthlyIb monthlyIb = QMonthlyIb.monthlyIb;

    public MonthlyIbRepositoryImpl(
            MonthlyIbJpaRepository monthlyIbJpaRepository,
            MonthlyIbPdfJpaRepository monthlyIbPdfJpaRepository,
            MonthlyIbThumbnailJpaRepository monthlyIbThumbnailJpaRepository
    ) {
        super(MonthlyIb.class);
        this.monthlyIbJpaRepository = monthlyIbJpaRepository;
        this.monthlyIbPdfJpaRepository = monthlyIbPdfJpaRepository;
        this.monthlyIbThumbnailJpaRepository = monthlyIbThumbnailJpaRepository;
    }

    @Override
    public Page<MonthlyIbSimpleResponseDto> findAllMonthlyIb(Pageable pageable, MonthlyIbSearchDto searchDto) {
        JPQLQuery<MonthlyIbSimpleResponseDto> query = getMonthlyIbJPQLQuery();
        String keyWord = searchDto.getKeyWord();
        if (keyWord != null && !keyWord.isEmpty()) {
            query.where(monthlyIb.title.containsIgnoreCase(keyWord));
        }
        List<MonthlyIbSimpleResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public MonthlyIb findMonthlyIbById(Long monthlyIbId) {
        return monthlyIbJpaRepository.findById(monthlyIbId).orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_MONTHLY_IB));
    }

    @Override
    public MonthlyIbResponseDto findMonthlyIbDtoById(Long monthlyIbId) {
        return MonthlyIbResponseDto.of(
                monthlyIbJpaRepository.findById(monthlyIbId).orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_MONTHLY_IB)),
                findMonthlyIbPdfFileByMonthlyIbId(monthlyIbId)
        );
    }

    @Override
    public MonthlyIbThumbnailFile findMonthlyIbThumbnailFileById(Long monthlyIbThumbnailFileId) {
        return monthlyIbThumbnailJpaRepository.findById(monthlyIbThumbnailFileId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    @Override
    public List<MonthlyIbPdfFileResponseDto> findMonthlyIbPdfFileByMonthlyIbId(Long monthlyIbId) {
        return monthlyIbPdfJpaRepository.findAllByMonthlyIbId(monthlyIbId).stream().map(MonthlyIbPdfFileResponseDto::of).toList();
    }

    @Override
    public MonthlyIb saveMonthlyIb(MonthlyIb monthlyIb) {
        return monthlyIbJpaRepository.save(monthlyIb);
    }

    @Override
    public void deleteMonthlyIb(Long monthlyIbId) {
        monthlyIbJpaRepository.deleteById(monthlyIbId);
    }

    @Override
    public MonthlyIbThumbnailFile saveMonthlyIbThumbnailFile(MonthlyIbThumbnailFile thumbnailFile) {
        return monthlyIbThumbnailJpaRepository.save(thumbnailFile);
    }

    @Override
    public MonthlyIbPdfFile saveMonthlyIbPdfFile(MonthlyIbPdfFile pdfFile) {
        return monthlyIbPdfJpaRepository.save(pdfFile);
    }

    @Override
    public void deleteMonthlyIbPdfFileByMonthlyIbId(Long monthlyIbId) {
        monthlyIbPdfJpaRepository.deleteAllByMonthlyIbId(monthlyIbId);
    }

    @Override
    public void deleteMonthlyIbThumbnailFileByMonthlyIbId(Long monthlyIbId) {
        monthlyIbThumbnailJpaRepository.deleteByMonthlyIbId(monthlyIbId);
    }

    private JPQLQuery<MonthlyIbSimpleResponseDto> getMonthlyIbJPQLQuery() {
        return from(monthlyIb)
                .select(
                        Projections.constructor(
                                MonthlyIbSimpleResponseDto.class,
                                monthlyIb.monthlyIbId,
                                monthlyIb.title,
                                monthlyIb.content,
                                monthlyIb.monthlyIbThumbnailFileId,
                                monthlyIb.monthlyIbThumbnailFileUrl
                        )
                );
    }

}

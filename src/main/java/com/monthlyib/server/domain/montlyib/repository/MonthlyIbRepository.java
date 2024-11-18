package com.monthlyib.server.domain.montlyib.repository;

import com.monthlyib.server.api.monthlyib.dto.MonthlyIbPdfFileResponseDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbResponseDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbSearchDto;
import com.monthlyib.server.api.monthlyib.dto.MonthlyIbSimpleResponseDto;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbPdfFile;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbThumbnailFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MonthlyIbRepository {

    Page<MonthlyIbSimpleResponseDto> findAllMonthlyIb(Pageable pageable, MonthlyIbSearchDto searchDto);

    MonthlyIb findMonthlyIbById(Long monthlyIbId);

    MonthlyIbResponseDto findMonthlyIbDtoById(Long monthlyIbId);

    MonthlyIbThumbnailFile findMonthlyIbThumbnailFileById(Long monthlyIbThumbnailFileId);

    List<MonthlyIbPdfFileResponseDto> findMonthlyIbPdfFileByMonthlyIbId(Long monthlyIbId);

    MonthlyIb saveMonthlyIb(MonthlyIb monthlyIb);

    void deleteMonthlyIb(Long monthlyIbId);

    MonthlyIbThumbnailFile saveMonthlyIbThumbnailFile(MonthlyIbThumbnailFile thumbnailFile);

    MonthlyIbPdfFile saveMonthlyIbPdfFile(MonthlyIbPdfFile pdfFile);

    void deleteMonthlyIbPdfFileByMonthlyIbId(Long monthlyIbId);

    void deleteMonthlyIbThumbnailFileByMonthlyIbId(Long monthlyIbId);

}

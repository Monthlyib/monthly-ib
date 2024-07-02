package com.monthlyib.server.api.monthlyib.dto;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIbThumbnailFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyIbResponseDto {

    private Long monthlyIbId;

    private String title;

    private String content;

    private Long monthlyIbThumbnailId;

    private String monthlyIbThumbnailUrl;

    private List<MonthlyIbPdfFileResponseDto> pdfFiles;

    public static MonthlyIbResponseDto of(
            MonthlyIb monthlyIb,
            List<MonthlyIbPdfFileResponseDto> pdfFiles
    ) {
        return MonthlyIbResponseDto.builder()
                .monthlyIbId(monthlyIb.getMonthlyIbId())
                .title(monthlyIb.getTitle())
                .monthlyIbThumbnailId(monthlyIb.getMonthlyIbThumbnailFileId())
                .monthlyIbThumbnailUrl(monthlyIb.getMonthlyIbThumbnailFileUrl())
                .pdfFiles(pdfFiles)
                .build();
    }

}

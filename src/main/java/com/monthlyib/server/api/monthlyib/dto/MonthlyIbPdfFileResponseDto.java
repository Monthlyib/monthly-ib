package com.monthlyib.server.api.monthlyib.dto;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIbPdfFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyIbPdfFileResponseDto {

    private Long fileId;

    private String fileName;

    private String fileUrl;

    public static MonthlyIbPdfFileResponseDto of(MonthlyIbPdfFile monthlyIbPdfFile) {
        return MonthlyIbPdfFileResponseDto.builder()
                .fileId(monthlyIbPdfFile.getMonthlyIbPdfFileId())
                .fileUrl(monthlyIbPdfFile.getUrl())
                .fileName(monthlyIbPdfFile.getFileName())
                .build();
    }

}

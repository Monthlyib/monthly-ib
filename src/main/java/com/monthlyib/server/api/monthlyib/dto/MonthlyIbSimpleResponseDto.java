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
public class MonthlyIbSimpleResponseDto {

    private Long monthlyIbId;

    private String title;

    private String content;

    private Long monthlyIbThumbnailId;

    private String monthlyIbThumbnailUrl;

    public static MonthlyIbSimpleResponseDto of(MonthlyIb monthlyIb) {
        return MonthlyIbSimpleResponseDto.builder()
                .monthlyIbId(monthlyIb.getMonthlyIbId())
                .title(monthlyIb.getTitle())
                .monthlyIbThumbnailId(monthlyIb.getMonthlyIbThumbnailFileId())
                .monthlyIbThumbnailUrl(monthlyIb.getMonthlyIbThumbnailFileUrl())
                .build();
    }

}

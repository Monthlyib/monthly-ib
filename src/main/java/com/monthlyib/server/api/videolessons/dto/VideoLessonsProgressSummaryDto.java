package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsProgressSummaryDto {

    private Long mainChapterId;

    private Long subChapterId;

    private Long positionSeconds;
}

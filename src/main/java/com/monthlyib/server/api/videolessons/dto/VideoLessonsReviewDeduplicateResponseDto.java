package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsReviewDeduplicateResponseDto {

    private int scannedReviewCount;

    private int duplicateGroupCount;

    private int deletedReviewCount;

    private int updatedVideoLessonsCount;
}

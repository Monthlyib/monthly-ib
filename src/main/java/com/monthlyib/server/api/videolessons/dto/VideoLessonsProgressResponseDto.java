package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsProgressResponseDto {

    private Long videoLessonsId;

    private Double progressPercent;

    private Long completedLessonCount;

    private Long totalLessonCount;

    private VideoLessonsProgressSummaryDto resumeTarget;

    private List<VideoLessonsLessonProgressDto> lessons;
}

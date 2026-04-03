package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.domain.videolessons.entity.VideoLessonsProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsLessonProgressDto {

    private Long mainChapterId;

    private Long subChapterId;

    private Long lastPositionSeconds;

    private Long durationSeconds;

    private Double progressPercent;

    private boolean completed;

    private LocalDateTime lastWatchedAt;

    public static VideoLessonsLessonProgressDto of(VideoLessonsProgress progress) {
        return VideoLessonsLessonProgressDto.builder()
                .mainChapterId(progress.getMainChapterId())
                .subChapterId(progress.getSubChapterId())
                .lastPositionSeconds(progress.getLastPositionSeconds())
                .durationSeconds(progress.getDurationSeconds())
                .progressPercent(progress.getProgressPercent())
                .completed(progress.isCompleted())
                .lastWatchedAt(progress.getLastWatchedAt())
                .build();
    }
}

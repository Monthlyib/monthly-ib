package com.monthlyib.server.domain.videolessons.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "video_lessons_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_video_lessons_progress_user_course_lesson",
                        columnNames = {"user_id", "video_lessons_id", "sub_chapter_id"}
                )
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsProgress extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsProgressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "video_lessons_id", nullable = false)
    private Long videoLessonsId;

    @Column(name = "main_chapter_id", nullable = false)
    private Long mainChapterId;

    @Column(name = "sub_chapter_id", nullable = false)
    private Long subChapterId;

    @Column(name = "last_position_seconds", nullable = false)
    private Long lastPositionSeconds;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "progress_percent", nullable = false)
    private Double progressPercent;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "last_watched_at", nullable = false)
    private LocalDateTime lastWatchedAt;

    public static VideoLessonsProgress create(
            Long userId,
            Long videoLessonsId,
            Long mainChapterId,
            Long subChapterId,
            Long lastPositionSeconds,
            Long durationSeconds,
            Double progressPercent,
            boolean completed
    ) {
        return VideoLessonsProgress.builder()
                .userId(userId)
                .videoLessonsId(videoLessonsId)
                .mainChapterId(mainChapterId)
                .subChapterId(subChapterId)
                .lastPositionSeconds(lastPositionSeconds)
                .durationSeconds(durationSeconds)
                .progressPercent(progressPercent)
                .completed(completed)
                .lastWatchedAt(LocalDateTime.now())
                .build();
    }

    public void updateProgress(
            Long mainChapterId,
            Long lastPositionSeconds,
            Long durationSeconds,
            Double progressPercent,
            boolean completed
    ) {
        this.mainChapterId = mainChapterId;
        this.lastPositionSeconds = lastPositionSeconds;
        this.durationSeconds = durationSeconds;
        this.progressPercent = progressPercent;
        this.completed = completed;
        this.lastWatchedAt = LocalDateTime.now();
    }

    public void restart() {
        this.lastPositionSeconds = 0L;
        this.progressPercent = 0D;
        this.completed = false;
        this.lastWatchedAt = LocalDateTime.now();
    }
}

package com.monthlyib.server.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUsageCourseDto {

    private Long videoLessonsId;

    private String title;

    private Double progressPercent;

    private long completedLessonCount;

    private long totalLessonCount;

    private LocalDateTime enrolledAt;

    private LocalDateTime lastWatchedAt;
}

package com.monthlyib.server.api.user.dto;

import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUsageResponseDto {

    private Long userId;

    private String username;

    private String nickName;

    private LocalDateTime lastAccessAt;

    private SubscribeUserResponseDto activeSubscribe;

    private long totalQuestionCount;

    private long waitingQuestionCount;

    private long completedQuestionCount;

    private long totalTutoringCount;

    private long waitingTutoringCount;

    private long confirmedTutoringCount;

    private long canceledTutoringCount;

    private long totalCourseCount;

    private List<UserUsageCourseDto> courses;
}

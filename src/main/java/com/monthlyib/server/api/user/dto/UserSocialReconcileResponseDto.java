package com.monthlyib.server.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialReconcileResponseDto {

    private int mergedUserCount;

    private int movedAiHistoryCount;

    private int movedIaRecommendationCount;

    private int movedQuizSessionCount;

    private int movedDescriptiveAnswerCount;

    private int movedVoiceFeedbackCount;

    private List<Long> mergedSourceUserIds;

    private List<Long> canonicalUserIds;
}

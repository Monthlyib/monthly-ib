package com.monthlyib.server.api.aihistory.dto;

import com.monthlyib.server.domain.aihistory.entity.AiToolHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolHistorySummaryResponseDto {

    private Long historyId;
    private String toolType;
    private String actionType;
    private String status;
    private String title;
    private String summary;
    private String subject;
    private String chapter;
    private String interestTopic;
    private Long relatedEntityId;
    private Integer score;
    private Integer maxScore;
    private Integer durationSeconds;
    private LocalDateTime createdAt;

    public static AiToolHistorySummaryResponseDto of(AiToolHistory entity) {
        return AiToolHistorySummaryResponseDto.builder()
                .historyId(entity.getAiToolHistoryId())
                .toolType(entity.getToolType().name())
                .actionType(entity.getActionType().name())
                .status(entity.getStatus().name())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .subject(entity.getSubject())
                .chapter(entity.getChapter())
                .interestTopic(entity.getInterestTopic())
                .relatedEntityId(entity.getRelatedEntityId())
                .score(entity.getScore())
                .maxScore(entity.getMaxScore())
                .durationSeconds(entity.getDurationSeconds())
                .createdAt(entity.getCreateAt())
                .build();
    }
}

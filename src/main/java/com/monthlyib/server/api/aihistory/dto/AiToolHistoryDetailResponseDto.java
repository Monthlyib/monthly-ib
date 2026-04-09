package com.monthlyib.server.api.aihistory.dto;

import com.monthlyib.server.domain.aihistory.entity.AiToolHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolHistoryDetailResponseDto {

    private Long historyId;
    private Long userId;
    private String username;
    private String nickName;
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
    private String requestPayloadJson;
    private String responsePayloadJson;
    private List<String> attachmentUrls;
    private LocalDateTime createdAt;

    public static AiToolHistoryDetailResponseDto of(AiToolHistory entity, List<String> attachmentUrls) {
        return AiToolHistoryDetailResponseDto.builder()
                .historyId(entity.getAiToolHistoryId())
                .userId(entity.getUser().getUserId())
                .username(entity.getUser().getUsername())
                .nickName(entity.getUser().getNickName())
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
                .requestPayloadJson(entity.getRequestPayloadJson())
                .responsePayloadJson(entity.getResponsePayloadJson())
                .attachmentUrls(attachmentUrls)
                .createdAt(entity.getCreateAt())
                .build();
    }
}

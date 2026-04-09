package com.monthlyib.server.domain.aihistory.model;

import com.monthlyib.server.domain.aihistory.entity.AiToolActionType;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolHistoryCreateCommand {

    private User user;
    private AiToolType toolType;
    private AiToolActionType actionType;
    private String title;
    private String summary;
    private String subject;
    private String chapter;
    private String interestTopic;
    private Long relatedEntityId;
    private Object requestPayload;
    private Object responsePayload;
    private List<String> attachmentUrls;
    private Integer score;
    private Integer maxScore;
    private Integer durationSeconds;
}

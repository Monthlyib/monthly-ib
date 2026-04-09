package com.monthlyib.server.domain.aihistory.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ai_tool_history",
        indexes = {
                @Index(name = "idx_ai_tool_history_user_created", columnList = "user_id, createAt"),
                @Index(name = "idx_ai_tool_history_tool_created", columnList = "toolType, createAt")
        }
)
public class AiToolHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiToolHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiToolType toolType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiToolActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiToolHistoryStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(length = 120)
    private String subject;

    @Column(length = 120)
    private String chapter;

    @Column(length = 255)
    private String interestTopic;

    private Long relatedEntityId;

    @Column(columnDefinition = "LONGTEXT")
    private String requestPayloadJson;

    @Column(columnDefinition = "LONGTEXT")
    private String responsePayloadJson;

    @Column(columnDefinition = "LONGTEXT")
    private String attachmentUrlsJson;

    private Integer score;

    private Integer maxScore;

    private Integer durationSeconds;
}

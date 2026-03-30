package com.monthlyib.server.domain.aidescriptive.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "ai_descriptive_answer")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveAnswer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT")
    private String answerText;

    private Long descriptiveQuestionId;

    @Column(columnDefinition = "LONGTEXT")
    private String feedbackEnglish;

    @Column(columnDefinition = "LONGTEXT")
    private String feedbackKorean;

    private Integer maxScore;

    @Column(columnDefinition = "LONGTEXT")
    private String modelAnswer;

    private Integer score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static AiDescriptiveAnswer create(Long questionId, String answerText, Integer maxScore, User user) {
        return AiDescriptiveAnswer.builder()
                .descriptiveQuestionId(questionId)
                .answerText(answerText)
                .maxScore(maxScore)
                .user(user)
                .build();
    }

    public void setFeedback(String feedbackEnglish, String feedbackKorean, Integer score, String modelAnswer) {
        this.feedbackEnglish = feedbackEnglish;
        this.feedbackKorean = feedbackKorean;
        this.score = score;
        this.modelAnswer = modelAnswer;
    }
}

package com.monthlyib.server.domain.aidescriptive.entity;

import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_descriptive_answer")
public class AiDescriptiveAnswer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String answerText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long descriptiveQuestionId; // ID of the descriptive question

    @Column(nullable = true)
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "feedback_english", columnDefinition = "LONGTEXT")
    private String feedbackEnglish;

    @Column(name = "feedback_korean", columnDefinition = "LONGTEXT")
    private String feedbackKorean;

    @Column(name = "model_answer", columnDefinition = "LONGTEXT")
    private String modelAnswer;
}

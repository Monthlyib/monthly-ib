package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "voice_feedback")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceFeedback extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    private String audioFilePath;

    private String author;

    private Long authorId;

    @Column(columnDefinition = "LONGTEXT")
    private String feedbackContent;

    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    @Column(columnDefinition = "LONGTEXT")
    private String assessmentRawJson;

    private Double pronunciationScore;

    private Double accuracyScore;

    private Double fluencyScore;

    private Double completenessScore;

    private Double prosodyScore;

    private Double speakingRateWpm;

    private Integer durationSeconds;

    private String iocTopic;

    private String scriptFilePath;

    private String workTitle;

    public static VoiceFeedback create(String author, Long authorId, String iocTopic, String workTitle) {
        return VoiceFeedback.builder()
                .author(author)
                .authorId(authorId)
                .iocTopic(iocTopic)
                .workTitle(workTitle)
                .build();
    }

    public void setAudioPath(String path) {
        this.audioFilePath = path;
    }

    public void setScriptPath(String path) {
        this.scriptFilePath = path;
    }

    public void setFeedbackContent(String content) {
        this.feedbackContent = content;
    }

    public void setSpeechAnalysis(
            String transcript,
            String assessmentRawJson,
            Double pronunciationScore,
            Double accuracyScore,
            Double fluencyScore,
            Double completenessScore,
            Double prosodyScore,
            Double speakingRateWpm,
            Integer durationSeconds
    ) {
        this.transcript = transcript;
        this.assessmentRawJson = assessmentRawJson;
        this.pronunciationScore = pronunciationScore;
        this.accuracyScore = accuracyScore;
        this.fluencyScore = fluencyScore;
        this.completenessScore = completenessScore;
        this.prosodyScore = prosodyScore;
        this.speakingRateWpm = speakingRateWpm;
        this.durationSeconds = durationSeconds;
    }
}

package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "voice_feedback")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceFeedback extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private String iocTopic;

    @Column(nullable = false)
    private String workTitle;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private Long authorId;

    // 대본 파일의 경로나 URL
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String scriptFilePath;

    // 녹음 파일의 경로나 URL
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String audioFilePath;

    // ChatGPT API로부터 받은 피드백 (예: 톤, 억양, 발음 등)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String feedbackContent;

    // 생성 요청 DTO를 기반으로 엔티티 생성
    public static VoiceFeedback create(AiioPostDto dto, Long authorId, String scriptFilePath, String audioFilePath, String feedbackContent) {
        return VoiceFeedback.builder()
                .iocTopic(dto.getIocTopic())
                .workTitle(dto.getWorkTitle())
                .author(dto.getAuthor())
                .authorId(authorId)
                .scriptFilePath(scriptFilePath)
                .audioFilePath(audioFilePath)
                .feedbackContent(feedbackContent)
                .build();
    }

    // 피드백 내용 수정 (필요한 경우)
    public VoiceFeedback update(AiioPatchDto dto) {
        this.feedbackContent = Optional.ofNullable(dto.getFeedbackContent()).orElse(this.feedbackContent);
        return this;
    }
}
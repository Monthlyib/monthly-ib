package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiioResponseDto {
    private Long feedbackId;
    private String iocTopic;
    private String workTitle;
    private String author;
    private Long authorId;
    private String scriptFilePath;
    private String audioFilePath;
    private String feedbackContent;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static AiioResponseDto of(VoiceFeedback voiceFeedback) {
        return AiioResponseDto.builder()
                .feedbackId(voiceFeedback.getFeedbackId())
                .iocTopic(voiceFeedback.getIocTopic())
                .workTitle(voiceFeedback.getWorkTitle())
                .author(voiceFeedback.getAuthor())
                .authorId(voiceFeedback.getAuthorId())
                .scriptFilePath(voiceFeedback.getScriptFilePath())
                .audioFilePath(voiceFeedback.getAudioFilePath())
                .feedbackContent(voiceFeedback.getFeedbackContent())
                .createAt(voiceFeedback.getCreateAt())
                .updateAt(voiceFeedback.getUpdateAt())
                .build();
    }
}
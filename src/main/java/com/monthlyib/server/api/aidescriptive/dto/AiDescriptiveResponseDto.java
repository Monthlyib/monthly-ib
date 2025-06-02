package com.monthlyib.server.api.aidescriptive.dto;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveResponseDto {

    private Long id;
    private String subject;
    private String chapter;
    private String question;
    private String imagePath;
    private Integer maxScore;

    public static AiDescriptiveResponseDto of(AiDescriptiveTest test) {
        return AiDescriptiveResponseDto.builder()
            .id(test.getId())
            .subject(test.getSubject())
            .chapter(test.getChapter())
            .question(test.getQuestion())
            .imagePath(test.getImagePath())
            .maxScore(test.getMaxScore())
            .build();
    }
}

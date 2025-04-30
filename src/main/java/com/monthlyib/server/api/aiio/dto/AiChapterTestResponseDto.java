package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiChapterTestResponseDto {

    private Long id;
    private String question;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String answer;
    private String subject;
    private String chapter;
    private String imagePath;

    public static AiChapterTestResponseDto of(AiChapterTest entity) {
        return AiChapterTestResponseDto.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .choiceA(entity.getChoiceA())
                .choiceB(entity.getChoiceB())
                .choiceC(entity.getChoiceC())
                .choiceD(entity.getChoiceD())
                .answer(entity.getAnswer())
                .subject(entity.getSubject())
                .chapter(entity.getChapter())
                .imagePath(entity.getImagePath())
                .build();
    }
}
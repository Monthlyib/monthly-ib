package com.monthlyib.server.api.aiio.dto;

import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChapterTestResponseDto {

    private Long id;
    private String subject;
    private String chapter;
    private String question;
    private String answer;
    private String choiceA;
    private String choiceB;
    private String choiceC;
    private String choiceD;
    private String imagePath;

    public static AiChapterTestResponseDto of(AiChapterTest entity) {
        AiChapterTestResponseDto dto = new AiChapterTestResponseDto();
        dto.setId(entity.getId());
        dto.setSubject(entity.getSubject());
        dto.setChapter(entity.getChapter());
        dto.setQuestion(entity.getQuestion());
        dto.setAnswer(entity.getAnswer());
        dto.setChoiceA(entity.getChoiceA());
        dto.setChoiceB(entity.getChoiceB());
        dto.setChoiceC(entity.getChoiceC());
        dto.setChoiceD(entity.getChoiceD());
        dto.setImagePath(entity.getImagePath());
        return dto;
    }
}

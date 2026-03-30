package com.monthlyib.server.api.aidescriptive.dto;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveResponseDto {

    private Long id;
    private String subject;
    private String chapter;
    private String question;
    private Integer maxScore;
    private String imagePath;
    private LocalDateTime createAt;

    public static AiDescriptiveResponseDto of(AiDescriptiveTest entity) {
        AiDescriptiveResponseDto dto = new AiDescriptiveResponseDto();
        dto.setId(entity.getId());
        dto.setSubject(entity.getSubject());
        dto.setChapter(entity.getChapter());
        dto.setQuestion(entity.getQuestion());
        dto.setMaxScore(entity.getMaxScore());
        dto.setImagePath(entity.getImagePath());
        dto.setCreateAt(entity.getCreateAt());
        return dto;
    }
}

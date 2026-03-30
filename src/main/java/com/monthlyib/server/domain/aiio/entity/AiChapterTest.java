package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.api.aiio.dto.AiChapterTestPostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "ai_chapter_test")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChapterTest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String answer;

    private String chapter;

    private String choiceA;

    private String choiceB;

    private String choiceC;

    private String choiceD;

    private String imagePath;

    @Column(columnDefinition = "LONGTEXT")
    private String question;

    private String subject;

    public static AiChapterTest create(AiChapterTestPostDto dto) {
        return AiChapterTest.builder()
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .question(dto.getQuestion())
                .answer(dto.getAnswer())
                .choiceA(dto.getChoiceA())
                .choiceB(dto.getChoiceB())
                .choiceC(dto.getChoiceC())
                .choiceD(dto.getChoiceD())
                .build();
    }
}

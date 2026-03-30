package com.monthlyib.server.domain.aidescriptive.entity;

import com.monthlyib.server.api.aidescriptive.dto.AiDescriptivePostDto;
import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "ai_descriptive_test")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptiveTest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chapter;

    private String imagePath;

    @Builder.Default
    private Integer maxScore = 20;

    @Column(columnDefinition = "LONGTEXT")
    private String question;

    private String subject;

    public static AiDescriptiveTest create(AiDescriptivePostDto dto) {
        return AiDescriptiveTest.builder()
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .question(dto.getQuestion())
                .maxScore(dto.getMaxScore() != null ? dto.getMaxScore() : 20)
                .build();
    }

    public void update(AiDescriptivePostDto dto) {
        this.subject = dto.getSubject();
        this.chapter = dto.getChapter();
        this.question = dto.getQuestion();
        if (dto.getMaxScore() != null) {
            this.maxScore = dto.getMaxScore();
        }
    }
}

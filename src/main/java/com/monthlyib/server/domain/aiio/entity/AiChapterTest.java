package com.monthlyib.server.domain.aiio.entity;

import com.monthlyib.server.audit.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_chapter_test")
public class AiChapterTest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String question;

    @Column(nullable = false)
    private String choiceA;

    @Column(nullable = false)
    private String choiceB;

    @Column(nullable = false)
    private String choiceC;

    @Column(nullable = false)
    private String choiceD;

    @Column(nullable = false)
    private String answer;

    @Column(columnDefinition = "LONGTEXT")
    private String imagePath;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String chapter;
}
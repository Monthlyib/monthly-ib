package com.monthlyib.server.domain.aiio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_session_id")
    private QuizSession quizSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_test_id")
    private AiChapterTest chapterTest;

    private String userAnswer;

    private boolean isCorrect;

    private int elapsedTime;
}

package com.monthlyib.server.domain.aiio.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "quiz_session_question")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_test_id")
    private AiChapterTest chapterTest;

    @Builder.Default
    private Integer elapsedTime = 0;

    @Builder.Default
    private boolean isCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_session_id")
    private QuizSession quizSession;

    private String userAnswer;

    public static QuizSessionQuestion create(QuizSession session, AiChapterTest test) {
        return QuizSessionQuestion.builder()
                .quizSession(session)
                .chapterTest(test)
                .elapsedTime(0)
                .isCorrect(false)
                .build();
    }

    public void submitAnswer(String answer, int elapsed) {
        this.userAnswer = answer;
        this.elapsedTime = elapsed;
        this.isCorrect = answer != null && answer.equals(this.chapterTest.getAnswer());
    }
}

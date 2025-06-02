package com.monthlyib.server.domain.aiio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;

public interface QuizSessionQuestionJpaRepository extends JpaRepository<QuizSessionQuestion, Long> {

    @Query("SELECT q FROM QuizSessionQuestion q JOIN FETCH q.chapterTest WHERE q.quizSession.id = :sessionId")
    List<QuizSessionQuestion> findByQuizSessionIdWithChapterTest(@Param("sessionId") Long sessionId);
    
    @Query("SELECT q FROM QuizSessionQuestion q JOIN FETCH q.chapterTest WHERE q.quizSession.id = :sessionId")
    List<QuizSessionQuestion> findWithChapterTestByQuizSessionId(@Param("sessionId") Long sessionId);
}

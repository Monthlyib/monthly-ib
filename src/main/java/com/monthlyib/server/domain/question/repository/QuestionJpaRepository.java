package com.monthlyib.server.domain.question.repository;

import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {

    long countByAuthorId(Long authorId);

    long countByAuthorIdAndQuestionStatus(Long authorId, QuestionStatus questionStatus);
}

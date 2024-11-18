package com.monthlyib.server.domain.answer.repository;

import com.monthlyib.server.domain.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerJpaRepository extends JpaRepository<Answer, Long> {

    Answer findByQuestionId(Long questionId);
}

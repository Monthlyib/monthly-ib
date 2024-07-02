package com.monthlyib.server.domain.question.repository;

import com.monthlyib.server.api.question.dto.AnswerResponseDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.api.question.dto.QuestionSearchDto;
import com.monthlyib.server.domain.answer.entity.Answer;
import com.monthlyib.server.domain.answer.entity.QAnswer;
import com.monthlyib.server.domain.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface QuestionRepository {

    Page<QuestionResponseDto> findAll(Pageable pageable, QuestionSearchDto searchDto);

    QuestionResponseDto findQuestionById(Long questionId);

    Page<QuestionResponseDto> findAllByUserId(Long userId, Pageable pageable, QuestionSearchDto searchDto);

    Optional<Question> findQuestionByQuestionId(Long questionId);

    Question saveQuestion(Question question);

    Optional<Answer> findAnswerByAnswerId(Long answerId);

    Answer saveAnswer(Answer answer);

    void deleteQuestion(Long questionId);

    void deleteAnswer(Long answerId);


}

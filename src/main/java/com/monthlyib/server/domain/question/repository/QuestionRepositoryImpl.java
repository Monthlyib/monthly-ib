package com.monthlyib.server.domain.question.repository;

import com.monthlyib.server.api.question.dto.AnswerResponseDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.api.question.dto.QuestionSearchDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.answer.entity.Answer;
import com.monthlyib.server.domain.answer.repository.AnswerJpaRepository;
import com.monthlyib.server.domain.question.entity.QQuestion;
import com.monthlyib.server.domain.question.entity.Question;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuestionRepositoryImpl extends QuerydslRepositorySupport implements QuestionRepository {

    private final AnswerJpaRepository answerJpaRepository;

    private final QuestionJpaRepository questionJpaRepository;


    public QuestionRepositoryImpl(AnswerJpaRepository answerJpaRepository, QuestionJpaRepository questionJpaRepository) {
        super(Question.class);
        this.answerJpaRepository = answerJpaRepository;
        this.questionJpaRepository = questionJpaRepository;
    }

    QQuestion question = QQuestion.question;


    @Override
    public Page<QuestionResponseDto> findAll(Pageable pageable, QuestionSearchDto searchDto) {
        JPQLQuery<QuestionResponseDto> query = getQuestionJpqlQuery();
        QuestionStatus status = searchDto.getQuestionStatus();
        String keyWord = searchDto.getKeyWord();
        if (status != null) {
            query.where(question.questionStatus.eq(status));
        }
        if (keyWord != null) {
            query.where(question.title.containsIgnoreCase(keyWord)
                    .or(question.content.containsIgnoreCase(keyWord)
                            .or(question.subject.containsIgnoreCase(keyWord))
                    )
            );
        }

        List<QuestionResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public QuestionResponseDto findQuestionById(Long questionId) {
        JPQLQuery<QuestionResponseDto> query = getQuestionJpqlQuery();
        query.where(question.questionId.eq(questionId));
        QuestionResponseDto result = query.fetchFirst();
        result.setAnswer(AnswerResponseDto.of(answerJpaRepository.findByQuestionId(questionId)));
        return result;
    }

    @Override
    public Page<QuestionResponseDto> findAllByUserId(Long userId, Pageable pageable, QuestionSearchDto searchDto) {
        JPQLQuery<QuestionResponseDto> query = getQuestionJpqlQuery();
        QuestionStatus status = searchDto.getQuestionStatus();
        String keyWord = searchDto.getKeyWord();
        if (status != null) {
            query.where(question.questionStatus.eq(status));
        }
        if (keyWord != null) {
            query.where(question.title.containsIgnoreCase(keyWord)
                    .or(question.content.containsIgnoreCase(keyWord)
                            .or(question.subject.containsIgnoreCase(keyWord))
                    )
            );
        }
        if (userId != null) {
            query.where(question.authorId.eq(userId));
        }

        List<QuestionResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public Optional<Question> findQuestionByQuestionId(Long questionId) {
        return questionJpaRepository.findById(questionId);
    }

    @Override
    public Question saveQuestion(Question question) {
        return questionJpaRepository.save(question);
    }

    @Override
    public Optional<Answer> findAnswerByAnswerId(Long answerId) {
        return answerJpaRepository.findById(answerId);
    }

    @Override
    public Answer saveAnswer(Answer answer) {
        return answerJpaRepository.save(answer);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question findQuestion = questionJpaRepository.findById(questionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        Long answerId = findQuestion.getAnswerId();
        if (answerId != 0) {
            answerJpaRepository.deleteById(answerId);
        }
        questionJpaRepository.deleteById(questionId);
    }

    @Override
    public void deleteAnswer(Long answerId) {
        answerJpaRepository.deleteById(answerId);
    }


    private JPQLQuery<QuestionResponseDto> getQuestionJpqlQuery() {
        return from(question)
                .select(
                        Projections.constructor(
                                QuestionResponseDto.class,
                                question.questionId,
                                question.title,
                                question.content,
                                question.subject,
                                question.authorId,
                                question.authorUsername,
                                question.authorNickName,
                                question.questionStatus,
                                question.createAt,
                                question.updateAt
                        )
                );
    }
}

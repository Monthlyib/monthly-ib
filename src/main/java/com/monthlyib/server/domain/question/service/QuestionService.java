package com.monthlyib.server.domain.question.service;


import com.monthlyib.server.api.question.dto.*;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.answer.entity.Answer;
import com.monthlyib.server.domain.question.entity.Question;
import com.monthlyib.server.domain.question.repository.QuestionRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.event.UserQuestionConfirmEvent;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;

    private final UserRepository userRepository;

    private final ApplicationEventPublisher publisher;

    public Page<QuestionResponseDto> findAllQuestion(int page, QuestionSearchDto searchDto) {
        return questionRepository.findAll(
                PageRequest.of(page, 10, Sort.by("createAt").descending()),
                searchDto
        );
    }

    public QuestionResponseDto findQuestion(Long questionId) {
        Question findQuestion = questionRepository.findQuestionByQuestionId(questionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        Long answerId = findQuestion.getAnswerId();
        Answer answer = getAnswer(answerId);
        return QuestionResponseDto.of(findQuestion, answer);
    }

    public Page<QuestionResponseDto> findAllQuestionByUserId(
            int page,
            QuestionSearchDto searchDto,
            Long userId
    ) {
        return questionRepository.findAllByUserId(
                userId,
                PageRequest.of(page, 10, Sort.by("createAt").descending()),
                searchDto
        );
    }

    private Answer getAnswer(Long answerId) {
        Answer answer = null;
        if (answerId != 0) {
            answer = questionRepository.findAnswerByAnswerId(answerId)
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        }
        return answer;
    }


    public QuestionResponseDto createQuestion(QuestionPostDto questionPostDto, Long userId) {
        User user = userRepository.findById(questionPostDto.getAuthorId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
        Question newQuestion = Question.create(questionPostDto, user);
        return QuestionResponseDto.of(questionRepository.saveQuestion(newQuestion), null);
    }

    public QuestionResponseDto updateQuestion(QuestionPatchDto questionPatchDto, Long userId) {
        Question findQuestion = questionRepository.findQuestionByQuestionId(questionPatchDto.getQuestionId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (!userId.equals(findQuestion.getAuthorId()) || findQuestion.getQuestionStatus().equals(QuestionStatus.COMPLETE)) {
            throw new ServiceLogicException(ErrorCode.COMPLETE_QUESTION);
        }
        Question update = findQuestion.update(questionPatchDto);
        Answer answer = getAnswer(update.getAnswerId());
        return QuestionResponseDto.of(questionRepository.saveQuestion(update), answer);
    }

    public void deleteQuestion(Long questionId, Long userId) {
        QuestionResponseDto findQuestion = questionRepository.findQuestionById(questionId);
        if (findQuestion.getQuestionStatus().equals(QuestionStatus.COMPLETE)) {
            throw new ServiceLogicException(ErrorCode.COMPLETE_QUESTION);
        }
        questionRepository.deleteQuestion(questionId);
    }

    public void createAnswer(AnswerPostDto answerPostDto, Long userId) {
        Long questionId = answerPostDto.getQuestionId();
        Question findQuestion = questionRepository.findQuestionByQuestionId(questionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        User author = userRepository.findById(findQuestion.getAuthorId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
        Answer newAnswer = Answer.create(answerPostDto, QuestionResponseDto.of(findQuestion, null), UserResponseDto.of(user, null));
        Answer answer = questionRepository.saveAnswer(newAnswer);
        findQuestion.setAnswerId(answer.getAnswerId());
        findQuestion.setQuestionStatus(QuestionStatus.COMPLETE);
        publisher.publishEvent(new UserQuestionConfirmEvent(this, author.getEmail(), author.getUsername()));
        questionRepository.saveQuestion(findQuestion);
    }

    public void updateAnswer(AnswerPatchDto answerPatchDto, Long userId) {
        Long answerId = answerPatchDto.getAnswerId();
        Answer answer = questionRepository.findAnswerByAnswerId(answerId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        Answer update = answer.update(answerPatchDto);
        questionRepository.saveAnswer(update);
    }

    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = questionRepository.findAnswerByAnswerId(answerId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (!answer.getAuthorId().equals(userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        Question findQuestion = questionRepository.findQuestionByQuestionId(answer.getQuestionId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        findQuestion.setAnswerId(0L);
        findQuestion.setQuestionStatus(QuestionStatus.ANSWER_WAIT);
        questionRepository.saveQuestion(findQuestion);
        questionRepository.deleteAnswer(answerId);

    }



}

package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.api.aiio.dto.QuizResultResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizSessionQuestionResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizSessionResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizStartDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aihistory.entity.AiToolActionType;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.aihistory.model.AiToolHistoryCreateCommand;
import com.monthlyib.server.domain.aihistory.service.AiToolHistoryService;
import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.entity.QuizSession;
import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import com.monthlyib.server.domain.aiio.repository.AiChapterTestJpaRepository;
import com.monthlyib.server.domain.aiio.repository.QuizSessionJpaRepository;
import com.monthlyib.server.domain.aiio.repository.QuizSessionQuestionJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QuizService {

    private final QuizSessionJpaRepository sessionRepo;
    private final QuizSessionQuestionJpaRepository questionRepo;
    private final AiChapterTestJpaRepository testRepo;
    private final AiToolHistoryService aiToolHistoryService;

    public QuizSessionResponseDto startQuiz(QuizStartDto dto, User user) {
        // Check if active session exists
        Optional<QuizSession> existingSession = sessionRepo.findByUserUserIdAndSubjectAndChapterAndIsSubmittedFalse(
                user.getUserId(), dto.getSubject(), dto.getChapter());

        if (existingSession.isPresent()) {
            QuizSession session = existingSession.get();
            List<QuizSessionQuestion> questions = questionRepo.findByQuizSessionId(session.getId());
            List<QuizSessionQuestionResponseDto> questionDtos = questions.stream()
                    .map(QuizSessionQuestionResponseDto::of)
                    .collect(Collectors.toList());
            return QuizSessionResponseDto.of(session, questionDtos);
        }

        // Find tests for subject+chapter (max 10)
        List<AiChapterTest> tests = testRepo.findBySubjectAndChapter(
                dto.getSubject(), dto.getChapter(), PageRequest.of(0, 10)).getContent();

        // Create new session
        int duration = dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 30;
        QuizSession session = QuizSession.create(dto.getSubject(), dto.getChapter(), duration, user);
        QuizSession savedSession = sessionRepo.save(session);

        // Create questions
        List<QuizSessionQuestion> questions = tests.stream()
                .map(test -> QuizSessionQuestion.create(savedSession, test))
                .collect(Collectors.toList());
        List<QuizSessionQuestion> savedQuestions = questionRepo.saveAll(questions);

        List<QuizSessionQuestionResponseDto> questionDtos = savedQuestions.stream()
                .map(QuizSessionQuestionResponseDto::of)
                .collect(Collectors.toList());

        QuizSessionResponseDto response = QuizSessionResponseDto.of(savedSession, questionDtos);
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("subject", dto.getSubject());
        requestPayload.put("chapter", dto.getChapter());
        requestPayload.put("durationMinutes", duration);
        aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                .user(user)
                .toolType(AiToolType.CHAPTER_TEST)
                .actionType(AiToolActionType.QUIZ_START)
                .title("AI Chapter Test 시작")
                .summary(String.format("%s / %s 문제 %d개", dto.getSubject(), dto.getChapter(), savedQuestions.size()))
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .relatedEntityId(savedSession.getId())
                .requestPayload(requestPayload)
                .responsePayload(response)
                .maxScore(savedQuestions.size())
                .build());

        return response;
    }

    @Transactional(readOnly = true)
    public QuizSessionResponseDto getActiveSession(String subject, String chapter, User user) {
        Optional<QuizSession> sessionOpt = sessionRepo.findByUserUserIdAndSubjectAndChapterAndIsSubmittedFalse(
                user.getUserId(), subject, chapter);

        if (sessionOpt.isEmpty()) {
            return null;
        }

        QuizSession session = sessionOpt.get();
        List<QuizSessionQuestion> questions = questionRepo.findByQuizSessionId(session.getId());
        List<QuizSessionQuestionResponseDto> questionDtos = questions.stream()
                .map(QuizSessionQuestionResponseDto::of)
                .collect(Collectors.toList());
        return QuizSessionResponseDto.of(session, questionDtos);
    }

    public QuizSessionResponseDto submitAnswer(Long sessionId, Long questionId, String userAnswer,
                                               Integer elapsedTime, User user) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        if (!session.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        QuizSessionQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        int elapsed = elapsedTime != null ? elapsedTime : 0;
        question.submitAnswer(userAnswer, elapsed);
        questionRepo.save(question);

        List<QuizSessionQuestion> questions = questionRepo.findByQuizSessionId(sessionId);
        List<QuizSessionQuestionResponseDto> questionDtos = questions.stream()
                .map(QuizSessionQuestionResponseDto::of)
                .collect(Collectors.toList());

        return QuizSessionResponseDto.of(session, questionDtos);
    }

    public QuizSessionResponseDto submitQuiz(Long sessionId, User user) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        if (!session.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        session.setSubmitted(true);
        QuizSession savedSession = sessionRepo.save(session);

        List<QuizSessionQuestion> questions = questionRepo.findByQuizSessionId(sessionId);
        List<QuizSessionQuestionResponseDto> questionDtos = questions.stream()
                .map(QuizSessionQuestionResponseDto::of)
                .collect(Collectors.toList());

        QuizSessionResponseDto response = QuizSessionResponseDto.of(savedSession, questionDtos);
        QuizResultResponseDto resultResponse = QuizResultResponseDto.of(savedSession, questions);
        int elapsedSeconds = questions.stream()
                .map(QuizSessionQuestion::getElapsedTime)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                .user(user)
                .toolType(AiToolType.CHAPTER_TEST)
                .actionType(AiToolActionType.QUIZ_RESULT)
                .title("AI Chapter Test 결과")
                .summary(String.format("%d/%d 정답", resultResponse.getCorrectCount(), resultResponse.getTotalQuestions()))
                .subject(savedSession.getSubject())
                .chapter(savedSession.getChapter())
                .relatedEntityId(savedSession.getId())
                .requestPayload(Map.of("quizSessionId", savedSession.getId()))
                .responsePayload(resultResponse)
                .score((int) Math.round(resultResponse.getScore()))
                .maxScore(100)
                .durationSeconds(elapsedSeconds)
                .build());

        return response;
    }

    @Transactional(readOnly = true)
    public QuizResultResponseDto getResult(Long sessionId, User user) {
        QuizSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        if (!session.getUser().getUserId().equals(user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }

        List<QuizSessionQuestion> questions = questionRepo.findByQuizSessionId(sessionId);
        return QuizResultResponseDto.of(session, questions);
    }
}

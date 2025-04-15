package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.api.aiio.dto.AiChapterTestDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizSessionStartRequestDto;
import com.monthlyib.server.api.aiio.dto.QuizSessionStartResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizAnswerStatusResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizResultResponseDto;
import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.entity.QuizSession;
import com.monthlyib.server.domain.aiio.entity.QuizSessionQuestion;
import com.monthlyib.server.domain.aiio.repository.AiChapterTestRepository;
import com.monthlyib.server.domain.aiio.repository.QuizSessionQuestionRepository;
import com.monthlyib.server.domain.aiio.repository.QuizSessionRepository;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChapterTestService {

    private final AiChapterTestRepository aiChapterTestRepository;
    private final FileService fileService;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;

    public AiChapterTestResponseDto createTest(AiChapterTestDto dto) {
        AiChapterTest test = AiChapterTest.builder()
                .question(dto.getQuestion())
                .choiceA(dto.getChoiceA())
                .choiceB(dto.getChoiceB())
                .choiceC(dto.getChoiceC())
                .choiceD(dto.getChoiceD())
                .answer(dto.getAnswer())
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .build();

        AiChapterTest saved = aiChapterTestRepository.save(test);

        return AiChapterTestResponseDto.of(saved);
    }

    public AiChapterTestResponseDto uploadImage(Long id, MultipartFile multipartFile) {
        AiChapterTest test = aiChapterTestRepository.findById(id);

        // 기존 이미지가 있다면 삭제
        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AICHAPTER_IMAGE);
        }

        // 새 이미지 저장
        String imagePath = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.AICHAPTER_IMAGE);
        test.setImagePath(imagePath);
        AiChapterTest saved = aiChapterTestRepository.save(test);

        return AiChapterTestResponseDto.of(saved);
    }
    
    public Page<AiChapterTestResponseDto> findBySubjectAndChapter(String subject, String chapter, int page) {
        Page<AiChapterTest> tests = aiChapterTestRepository.findBySubjectAndChapter(subject, chapter, PageRequest.of(page, 6, Sort.by("createAt").descending()));
        return tests.map(AiChapterTestResponseDto::of);
    }

    public AiChapterTestResponseDto findById(Long id) {
        AiChapterTest test = aiChapterTestRepository.findById(id);
        return AiChapterTestResponseDto.of(test);
    }

    public AiChapterTestResponseDto updateTest(Long id, AiChapterTestDto dto) {
        AiChapterTest test = aiChapterTestRepository.findById(id);

        test.setQuestion(dto.getQuestion());
        test.setChoiceA(dto.getChoiceA());
        test.setChoiceB(dto.getChoiceB());
        test.setChoiceC(dto.getChoiceC());
        test.setChoiceD(dto.getChoiceD());
        test.setAnswer(dto.getAnswer());
        test.setSubject(dto.getSubject());
        test.setChapter(dto.getChapter());

        AiChapterTest updated = aiChapterTestRepository.save(test);
        return AiChapterTestResponseDto.of(updated);
    }

    public void deleteTest(Long id) {
        AiChapterTest test = aiChapterTestRepository.findById(id);
        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AICHAPTER_IMAGE);
        }
        aiChapterTestRepository.delete(test);
    }

    public QuizSessionStartResponseDto startQuizSession(QuizSessionStartRequestDto request, User user) {
        List<QuizSession> previousSessions = quizSessionRepository.findAllByUserIdAndIsSubmittedFalse(user.getUserId()).stream()
                .filter(s -> s.getSubject().equals(request.getSubject()) && s.getChapter().equals(request.getChapter()))
                .toList();

        for (QuizSession session : previousSessions) {
            List<QuizSessionQuestion> relatedQuestions = quizSessionQuestionRepository.findByQuizSessionId(session.getId());
            relatedQuestions.forEach(quizSessionQuestionRepository::delete);
            quizSessionRepository.delete(session);
        }

        List<AiChapterTest> availableQuestions = aiChapterTestRepository.findAllBySubjectAndChapter(request.getSubject(), request.getChapter());

        if (availableQuestions.size() < request.getQuestionCount()) {
            throw new IllegalArgumentException("문제 수가 부족하여 퀴즈를 생성할 수 없습니다.");
        }

        Collections.shuffle(availableQuestions);
        List<AiChapterTest> selectedQuestions = availableQuestions.subList(0, request.getQuestionCount());

        QuizSession session = QuizSession.builder()
                .user(user)
                .subject(request.getSubject())
                .chapter(request.getChapter())
                .startedAt(LocalDateTime.now())
                .durationMinutes(request.getDurationMinutes())
                .isSubmitted(false)
                .build();

        QuizSession savedSession = quizSessionRepository.save(session);

        selectedQuestions.forEach(q -> {
            QuizSessionQuestion quizQuestion = QuizSessionQuestion.builder()
                    .quizSession(savedSession)
                    .chapterTest(q)
                    .isCorrect(false)
                    .build();
            quizSessionQuestionRepository.save(quizQuestion);
        });

        return QuizSessionStartResponseDto.of(savedSession, selectedQuestions);
    }

    public QuizSessionStartResponseDto findActiveQuizSession(User user, String subject, String chapter) {
        List<QuizSession> sessions = quizSessionRepository.findAllByUserIdAndIsSubmittedFalse(user.getUserId()).stream()
                .filter(s -> s.getSubject().equals(subject) && s.getChapter().equals(chapter))
                .toList();

        if (sessions.isEmpty()) {
            return null;
        }

        QuizSession active = sessions.get(0);
        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findByQuizSessionId(active.getId());

        List<AiChapterTest> chapterTests = questions.stream()
                .map(QuizSessionQuestion::getChapterTest)
                .toList();

        return QuizSessionStartResponseDto.of(active, chapterTests);
    }

    public void submitAnswer(Long quizSessionId, Long questionId, String userAnswer, int elapsedTime) {
        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findByQuizSessionId(quizSessionId);

        QuizSessionQuestion target = questions.stream()
                .filter(q -> q.getChapterTest().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

        target.setUserAnswer(userAnswer);
        target.setCorrect(userAnswer.equals(target.getChapterTest().getAnswer()));
        target.setElapsedTime(elapsedTime);

        quizSessionQuestionRepository.save(target);
    }

    public void submitQuizSession(Long quizSessionId) {
        QuizSession session = quizSessionRepository.findById(quizSessionId);
        session.setSubmitted(true);
        quizSessionRepository.save(session);
    }

    public QuizAnswerStatusResponseDto getAnswerStatus(Long quizSessionId, Long questionId) {
        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findByQuizSessionId(quizSessionId);

        QuizSessionQuestion target = questions.stream()
                .filter(q -> q.getChapterTest().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없습니다."));

        return QuizAnswerStatusResponseDto.builder()
                .userAnswer(target.getUserAnswer())
                .elapsedTime(target.getElapsedTime())
                .build();
    }

    public QuizResultResponseDto getQuizResult(Long quizSessionId) {
        QuizSession session = quizSessionRepository.findById(quizSessionId);
        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findByQuizSessionId(quizSessionId);

        long correct = questions.stream().filter(QuizSessionQuestion::isCorrect).count();
        long totalTime = questions.stream().mapToInt(QuizSessionQuestion::getElapsedTime).sum();

        return QuizResultResponseDto.builder()
                .quizSessionId(session.getId())
                .subject(session.getSubject())
                .chapter(session.getChapter())
                .totalQuestions(questions.size())
                .correctAnswers((int) correct)
                .totalTimeSeconds((int) totalTime)
                .submittedAt(LocalDateTime.now())
                .build();
    }
}

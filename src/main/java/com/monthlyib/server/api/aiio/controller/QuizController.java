package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aiio.dto.QuizResultResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizSessionResponseDto;
import com.monthlyib.server.api.aiio.dto.QuizStartDto;
import com.monthlyib.server.domain.aiio.service.QuizService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/start")
    public ResponseDto<QuizSessionResponseDto> startQuiz(
            @RequestBody QuizStartDto dto,
            @UserSession User user) {
        QuizSessionResponseDto result = quizService.startQuiz(dto, user);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/active")
    public ResponseDto<QuizSessionResponseDto> getActiveSession(
            @RequestParam String subject,
            @RequestParam String chapter,
            @UserSession User user) {
        QuizSessionResponseDto result = quizService.getActiveSession(subject, chapter, user);
        return ResponseDto.of(result, Result.ok());
    }

    @PatchMapping("/answer")
    public ResponseDto<QuizSessionResponseDto> submitAnswer(
            @RequestParam Long quizSessionId,
            @RequestParam Long questionId,
            @RequestParam String userAnswer,
            @RequestParam(required = false) Integer elapsedTime,
            @UserSession User user) {
        QuizSessionResponseDto result = quizService.submitAnswer(quizSessionId, questionId, userAnswer, elapsedTime, user);
        return ResponseDto.of(result, Result.ok());
    }

    @PatchMapping("/submit/{quizSessionId}")
    public ResponseDto<QuizSessionResponseDto> submitQuiz(
            @PathVariable Long quizSessionId,
            @UserSession User user) {
        QuizSessionResponseDto result = quizService.submitQuiz(quizSessionId, user);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/result/{quizSessionId}")
    public ResponseDto<QuizResultResponseDto> getResult(
            @PathVariable Long quizSessionId,
            @UserSession User user) {
        QuizResultResponseDto result = quizService.getResult(quizSessionId, user);
        return ResponseDto.of(result, Result.ok());
    }
}

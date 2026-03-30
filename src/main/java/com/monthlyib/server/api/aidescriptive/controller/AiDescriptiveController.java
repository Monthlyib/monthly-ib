package com.monthlyib.server.api.aidescriptive.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveAnswerResponseDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveAnswerSubmitDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptivePostDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.domain.aidescriptive.service.AiDescriptiveService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiDescriptiveController {

    private final AiDescriptiveService aiDescriptiveService;

    // ==================== /api/descriptive-test endpoints ====================

    @PostMapping("/api/descriptive-test")
    public ResponseDto<AiDescriptiveResponseDto> create(@RequestBody AiDescriptivePostDto dto) {
        AiDescriptiveResponseDto result = aiDescriptiveService.create(dto);
        return ResponseDto.of(result, Result.ok());
    }

    @PatchMapping("/api/descriptive-test/{id}")
    public ResponseDto<AiDescriptiveResponseDto> update(
            @PathVariable Long id,
            @RequestBody AiDescriptivePostDto dto) {
        AiDescriptiveResponseDto result = aiDescriptiveService.update(id, dto);
        return ResponseDto.of(result, Result.ok());
    }

    @PostMapping("/api/descriptive-test/image/{id}")
    public ResponseDto<AiDescriptiveResponseDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        AiDescriptiveResponseDto result = aiDescriptiveService.uploadImage(id, image);
        return ResponseDto.of(result, Result.ok());
    }

    @DeleteMapping("/api/descriptive-test/image/{id}")
    public ResponseDto<Result> deleteImage(@PathVariable Long id) {
        aiDescriptiveService.deleteImage(id);
        return ResponseDto.of(Result.ok());
    }

    @GetMapping("/api/descriptive-test/list")
    public PageResponseDto<Page<AiDescriptiveResponseDto>> findAll(
            @RequestParam String subject,
            @RequestParam String chapter,
            @RequestParam(defaultValue = "0") int page) {
        Page<AiDescriptiveResponseDto> result = aiDescriptiveService.findAll(subject, chapter, page);
        return PageResponseDto.of(result, result, Result.ok());
    }

    @DeleteMapping("/api/descriptive-test/{id}")
    public ResponseDto<Result> delete(@PathVariable Long id) {
        aiDescriptiveService.delete(id);
        return ResponseDto.of(Result.ok());
    }

    @GetMapping("/api/descriptive-test/{id}")
    public ResponseDto<AiDescriptiveResponseDto> findById(@PathVariable Long id) {
        AiDescriptiveResponseDto result = aiDescriptiveService.findById(id);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/api/descriptive-test")
    public ResponseDto<AiDescriptiveResponseDto> findBySubjectAndChapter(
            @RequestParam String subject,
            @RequestParam String chapter) {
        AiDescriptiveResponseDto result = aiDescriptiveService.findBySubjectAndChapter(subject, chapter);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/api/descriptive-test/start")
    public ResponseDto<AiDescriptiveResponseDto> getQuestion(
            @RequestParam String subject,
            @RequestParam String chapter) {
        AiDescriptiveResponseDto result = aiDescriptiveService.getQuestion(subject, chapter);
        return ResponseDto.of(result, Result.ok());
    }

    @PostMapping("/api/descriptive-test/submit")
    public ResponseDto<AiDescriptiveAnswerResponseDto> submitAnswer(
            @RequestBody AiDescriptiveAnswerSubmitDto dto,
            @UserSession User user) {
        AiDescriptiveAnswerResponseDto result = aiDescriptiveService.submitAnswer(dto, user);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/api/descriptive-test/result/{answerId}")
    public ResponseDto<AiDescriptiveAnswerResponseDto> getResult(
            @PathVariable Long answerId,
            @UserSession User user) {
        AiDescriptiveAnswerResponseDto result = aiDescriptiveService.getResult(answerId);
        return ResponseDto.of(result, Result.ok());
    }

    @GetMapping("/api/descriptive-test/answer-feedback/{answerId}")
    public ResponseDto<AiDescriptiveAnswerResponseDto> generateFeedback(
            @PathVariable Long answerId,
            @UserSession User user) {
        AiDescriptiveAnswerResponseDto result = aiDescriptiveService.generateFeedback(answerId);
        return ResponseDto.of(result, Result.ok());
    }

    // ==================== /api/descriptive-quiz endpoints ====================

    @GetMapping("/api/descriptive-quiz/active")
    public ResponseDto<Object> getActiveDescriptiveSession(
            @RequestParam String subject,
            @RequestParam String chapter,
            @UserSession User user) {
        return ResponseDto.of(null, Result.ok());
    }
}

package com.monthlyib.server.api.aidescriptive.controller;
import java.util.HashMap;
import java.util.Map;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.domain.aidescriptive.service.AiDescriptiveService;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveTestDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.api.aidescriptive.dto.SubmitDescriptiveAnswerDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.monthlyib.server.dto.PageResponseDto; // Added import

@Controller
@RequestMapping("/api/descriptive-test")
@RequiredArgsConstructor
public class AiDescriptiveApiController {

    private final AiDescriptiveService aiDescriptiveService;

    @PostMapping
    public ResponseEntity<ResponseDto<?>> createDescriptiveTest(
            @RequestBody AiDescriptiveTestDto dto,
            @UserSession User user) {
        AiDescriptiveResponseDto response = aiDescriptiveService.createTest(dto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<ResponseDto<?>> uploadDescriptiveImage(
            @PathVariable("id") Long id,
            @RequestParam("image") MultipartFile multipartFile,
            @UserSession User user) {
        AiDescriptiveResponseDto response = aiDescriptiveService.uploadImage(id, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<ResponseDto<?>> deleteDescriptiveImage(
            @PathVariable("id") Long id,
            @UserSession User user) {
        AiDescriptiveResponseDto response = aiDescriptiveService.deleteImage(id);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
    @GetMapping("/list")
    public ResponseEntity<PageResponseDto<?>> getDescriptiveTestsBySubjectAndChapter(
            @RequestParam("subject") String subject,
            @RequestParam("chapter") String chapter,
            @RequestParam("page") int page,
            @UserSession User user) {

        var pageResult = aiDescriptiveService.findBySubjectAndChapter(subject, chapter, page);
        return ResponseEntity.ok(PageResponseDto.of(pageResult, pageResult.getContent(), Result.ok()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> patchDescriptiveTest(
            @PathVariable("id") Long id,
            @RequestBody AiDescriptiveTestDto dto,
            @UserSession User user) {
        AiDescriptiveResponseDto response = aiDescriptiveService.updateTest(id, dto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> deleteDescriptiveTest(
            @PathVariable("id") Long id,
            @UserSession User user) {
        aiDescriptiveService.deleteTest(id);
        return ResponseEntity.ok(ResponseDto.of("삭제가 완료되었습니다.", Result.ok()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> getDescriptiveTestById(
            @PathVariable("id") Long id,
            @UserSession User user) {
        AiDescriptiveResponseDto response = AiDescriptiveResponseDto.of(aiDescriptiveService.findById(id));
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @GetMapping("/start")
    public ResponseEntity<ResponseDto<?>> getSingleDescriptiveTest(
            @RequestParam("subject") String subject,
            @RequestParam("chapter") String chapter,
            @UserSession User user) {
        AiDescriptiveResponseDto response = AiDescriptiveResponseDto.of(aiDescriptiveService.findBySubjectAndChapterOnce(subject, chapter));
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @PostMapping("/submit")
    public ResponseEntity<ResponseDto<?>> submitDescriptiveAnswer(
            @RequestBody SubmitDescriptiveAnswerDto request,
            @UserSession User user) {
        Long answerId = aiDescriptiveService.submitAnswer(request, user);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("answerId", answerId);
        return ResponseEntity.ok(ResponseDto.of(responseMap, Result.ok()));
    }

    @GetMapping("/result/{answerId}")
    public ResponseEntity<ResponseDto<?>> getDescriptiveAnswerResult(
            @PathVariable("answerId") Long answerId,
            @UserSession User user) {
        var result = aiDescriptiveService.getAnswerResult(answerId);
        return ResponseEntity.ok(ResponseDto.of(result, Result.ok()));
    }
}
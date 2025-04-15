package com.monthlyib.server.api.aiio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping; // Added import
import org.springframework.web.bind.annotation.DeleteMapping; // Added import
import java.util.List; // Added import

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.api.aiio.dto.AiioResponseDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestResponseDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestSearchDto; // Added import

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.service.AiioService;
import com.monthlyib.server.domain.aiio.service.AiChapterTestService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.dto.PageResponseDto; // Added import

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AiioApiController {

    private final AiioService aiioService;

    private final AiChapterTestService aiChapterTestService;

    /**
     * 프론트엔드에서 전송한 데이터를 받아 파일 업로드, ChatGPT API 호출, 피드백 저장을 수행한 후
     * 생성된 VoiceFeedback 엔티티를 AiioResponseDto로 변환하여 반환합니다.
     */
    @PostMapping(value = "/api/aiio", consumes = "multipart/form-data")
    public ResponseEntity<ResponseDto<?>> postAiioFeedback(
            @ModelAttribute AiioPostDto postDto,
            @UserSession User user) {
        VoiceFeedback voiceFeedback = aiioService.createFeedback(postDto, user);
        AiioResponseDto responseDto = AiioResponseDto.of(voiceFeedback);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }

    /**
     * 피드백 수정 요청을 처리합니다.
     */
    @PatchMapping("/api/aiio")
    public ResponseEntity<ResponseDto<?>> patchAiioFeedback(
            @ModelAttribute AiioPatchDto patchDto,
            @UserSession User user) {
        VoiceFeedback voiceFeedback = aiioService.updateFeedback(patchDto, user);
        AiioResponseDto responseDto = AiioResponseDto.of(voiceFeedback);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }

    /**
     * 챕터 테스트 문제를 생성합니다.
     */
    @PostMapping("/api/chapter-test")
    public ResponseEntity<ResponseDto<?>> postAiChapterTest(
            @RequestBody AiChapterTestDto testDto,
            @UserSession User user) {
        AiChapterTestResponseDto responseDto = aiChapterTestService.createTest(testDto);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }

    @PostMapping("/api/chapter-test/image/{id}")
    public ResponseEntity<ResponseDto<?>> postAiChapterTestImage(
            @PathVariable("id") Long id,
            @RequestParam("image") MultipartFile multipartFile,
            @UserSession User user) {
        AiChapterTestResponseDto response = aiChapterTestService.uploadImage(id, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @GetMapping("/api/chapter-test/list")
    public ResponseEntity<PageResponseDto<?>> getChapterTestsBySubjectAndChapter(
            @RequestParam("subject") String subject,
            @RequestParam("chapter") String chapter,
            @RequestParam("page") int page,
            @UserSession User user) {
                
        var pageResult = aiChapterTestService.findBySubjectAndChapter(subject, chapter, page);
        return ResponseEntity.ok(PageResponseDto.of(pageResult, pageResult.getContent(), Result.ok()));
    }

    @PatchMapping("/api/chapter-test/{id}")
    public ResponseEntity<ResponseDto<?>> patchAiChapterTest(
            @PathVariable("id") Long id,
            @RequestBody AiChapterTestDto testDto,
            @UserSession User user) {
        AiChapterTestResponseDto updated = aiChapterTestService.updateTest(id, testDto);
        return ResponseEntity.ok(ResponseDto.of(updated, Result.ok()));
    }

    @DeleteMapping("/api/chapter-test/{id}")
    public ResponseEntity<ResponseDto<?>> deleteAiChapterTest(
            @PathVariable("id") Long id,
            @UserSession User user) {
        aiChapterTestService.deleteTest(id);
        return ResponseEntity.ok(ResponseDto.of("삭제가 완료되었습니다.", Result.ok()));
    }

    @GetMapping("/api/chapter-test/{id}")
    public ResponseEntity<ResponseDto<?>> getAiChapterTestById(
            @PathVariable("id") Long id,
            @UserSession User user) {
        AiChapterTestResponseDto responseDto = aiChapterTestService.findById(id);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }

}
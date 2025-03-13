package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.api.aiio.dto.AiioResponseDto;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.service.AiioService;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class AiioApiController {

    private final AiioService aiioService;

    /**
     * 프론트엔드에서 전송한 데이터를 받아 파일 업로드, ChatGPT API 호출, 피드백 저장을 수행한 후
     * 생성된 VoiceFeedback 엔티티를 AiioResponseDto로 변환하여 반환합니다.
     */
    @PostMapping(value = "/api/aiio", consumes = "multipart/form-data")
    public ResponseEntity<ResponseDto<?>> postAiioFeedback(
            @ModelAttribute AiioPostDto postDto,
            @RequestPart("scriptFile") MultipartFile scriptFile,
            @RequestPart("audioFile") MultipartFile audioFile,
            User user) {
    
        VoiceFeedback voiceFeedback = aiioService.createFeedback(postDto, scriptFile, audioFile, user);
        // VoiceFeedback을 AiioResponseDto로 변환
        AiioResponseDto responseDto = AiioResponseDto.of(voiceFeedback);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }

    /**
     * 피드백 수정 요청을 처리합니다.
     */
    @PatchMapping("/api/aiio")
    public ResponseEntity<ResponseDto<?>> patchAiioFeedback(
            @ModelAttribute AiioPatchDto patchDto,
            User user) {

        VoiceFeedback voiceFeedback = aiioService.updateFeedback(patchDto, user);
        // 수정된 VoiceFeedback을 AiioResponseDto로 변환
        AiioResponseDto responseDto = AiioResponseDto.of(voiceFeedback);
        return ResponseEntity.ok(ResponseDto.of(responseDto, Result.ok()));
    }
}
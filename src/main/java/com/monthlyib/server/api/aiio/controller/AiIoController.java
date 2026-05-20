package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.domain.aiio.service.AiIoVoiceFeedbackService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/aiio")
@RequiredArgsConstructor
@Slf4j
public class AiIoController {

    private final AiIoVoiceFeedbackService aiIoVoiceFeedbackService;

    @PostMapping
    public ResponseDto<Map<String, Object>> createVoiceFeedback(
            @RequestPart(required = false) MultipartFile audioFile,
            @RequestPart(required = false) MultipartFile scriptFile,
            @RequestPart String iocTopic,
            @RequestPart String workTitle,
            @RequestPart(required = false) Integer durationSeconds,
            @UserSession User user) {

        Map<String, Object> result = aiIoVoiceFeedbackService.createVoiceFeedback(
                audioFile,
                scriptFile,
                iocTopic,
                workTitle,
                durationSeconds,
                user
        );

        return ResponseDto.of(result, Result.ok());
    }
}

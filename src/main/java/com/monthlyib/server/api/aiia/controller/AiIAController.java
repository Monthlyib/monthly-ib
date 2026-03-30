package com.monthlyib.server.api.aiia.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aiia.dto.AiIAEnglishChatRequestDto;
import com.monthlyib.server.api.aiia.dto.AiIARequestDto;
import com.monthlyib.server.api.aiia.dto.AiIATopicGuideRequestDto;
import com.monthlyib.server.domain.aiia.service.AiIAService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/IA")
@RequiredArgsConstructor
@Slf4j
public class AiIAController {

    private final AiIAService aiIAService;

    @PostMapping("/recommend-topic")
    public ResponseDto<Map<String, Object>> recommendTopic(
            @RequestBody AiIARequestDto requestDto,
            @UserSession User user) {
        Map<String, Object> result = aiIAService.recommendTopics(
                requestDto.getSubject(),
                requestDto.getInterest(),
                user);
        return ResponseDto.of(result, Result.ok());
    }

    @PostMapping("/topic-guide")
    public ResponseDto<Map<String, Object>> topicGuide(
            @RequestBody AiIATopicGuideRequestDto requestDto,
            @UserSession User user) {
        Map<String, Object> result = aiIAService.createTopicGuide(
                requestDto.getSubject(),
                requestDto.getInterestTopic(),
                requestDto.getTopic(),
                user);
        return ResponseDto.of(result, Result.ok());
    }

    @PostMapping("/english-chat")
    public ResponseDto<Map<String, Object>> englishChat(
            @RequestBody AiIAEnglishChatRequestDto requestDto,
            @UserSession User user) {
        Map<String, Object> result = aiIAService.englishChat(
                requestDto.getSubject(),
                requestDto.getPrompt(),
                requestDto.getTextType(),
                requestDto.getResponseMode(),
                user);
        return ResponseDto.of(result, Result.ok());
    }
}

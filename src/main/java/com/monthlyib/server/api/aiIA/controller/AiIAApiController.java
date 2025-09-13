package com.monthlyib.server.api.aiIA.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aiIA.dto.RecommendTopicRequestDto;
import com.monthlyib.server.api.aiIA.dto.TopicGuideRequestDto;
import com.monthlyib.server.domain.aiia.service.AiIAService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/ai/IA")
@RequiredArgsConstructor
public class AiIAApiController {

    private final AiIAService aiIAService;

    @PostMapping("/recommend-topic")
    public ResponseEntity<ResponseDto<?>> recommendTopic(
            @RequestBody RecommendTopicRequestDto request,
            @UserSession User user
    ) {
        Map<String, Object> recommendations = aiIAService.recommendTopics(request.getSubject(), request.getInterest(), user);
        return ResponseEntity.ok(ResponseDto.of(recommendations, Result.ok()));
    }
    
    @PostMapping("/topic-guide")
    public ResponseEntity<ResponseDto<?>> createTopicGuide(
            @RequestBody TopicGuideRequestDto request,
            @UserSession User user
    ) {
        Map<String, Object> resp = aiIAService.createTopicGuide(
                request.getSubject(),
                request.getInterestTopic(),
                request.getTopic(),
                user
        );
        return ResponseEntity.ok(ResponseDto.of(resp, Result.ok()));
    }
    
}

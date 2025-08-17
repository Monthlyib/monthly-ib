package com.monthlyib.server.api.aiIA.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.domain.aiia.service.AiIAService;
import com.monthlyib.server.api.aiIA.dto.RecommendTopicRequestDto;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
}

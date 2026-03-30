package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.domain.aiia.service.OpenAiAssistantService;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.repository.VoiceFeedbackJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/aiio")
@RequiredArgsConstructor
@Slf4j
public class AiIoController {

    private final VoiceFeedbackJpaRepository feedbackRepo;
    private final OpenAiAssistantService openAiService;
    private final FileService fileService;

    @Value("${OPENAI_API_KEY}")
    private String openAiKey;

    @PostMapping
    public ResponseDto<Map<String, Object>> createVoiceFeedback(
            @RequestPart(required = false) MultipartFile audioFile,
            @RequestPart String iocTopic,
            @RequestPart String workTitle,
            @UserSession User user) {

        VoiceFeedback feedback = VoiceFeedback.create(
                user.getNickName(),
                user.getUserId(),
                iocTopic,
                workTitle);

        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = fileService.saveMultipartFileForAws(audioFile, AwsProperty.STORAGE, "aiio-audio/");
            feedback.setAudioPath(audioUrl);
        }

        String prompt = "You are an IB Language A examiner evaluating an IOC (Individual Oral Commentary) performance. " +
                "Work: " + workTitle + ". " +
                "Topic: " + iocTopic + ". " +
                "Please provide detailed feedback covering: " +
                "1) Knowledge and understanding of the text, " +
                "2) Interpretation and personal response, " +
                "3) Language use and structure, " +
                "4) Delivery and presentation. " +
                "Be specific and constructive.";

        String feedbackContent = openAiService.chatCompletion("You are an expert IB examiner.", prompt);
        feedback.setFeedbackContent(feedbackContent);

        VoiceFeedback saved = feedbackRepo.save(feedback);

        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", saved.getFeedbackId());
        result.put("feedbackContent", saved.getFeedbackContent());

        return ResponseDto.of(result, Result.ok());
    }
}

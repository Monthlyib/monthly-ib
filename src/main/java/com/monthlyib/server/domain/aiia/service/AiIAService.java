package com.monthlyib.server.domain.aiia.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;
import com.monthlyib.server.domain.aiia.repository.AiIARecommendationJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiIAService {

    private final AiIARecommendationJpaRepository repository;
    private final OpenAiAssistantService openAiService;

    @Value("${CHATGPT_ASSISTANT_KEY}")
    private String assistantKey;

    private final Gson gson = new Gson();

    public Map<String, Object> recommendTopics(String subject, String interest, User user) {
        try {
            String prompt = "You are an IB (International Baccalaureate) academic advisor. " +
                    "The student is studying subject: " + subject + ". " +
                    "Their interest/hobby is: " + interest + ". " +
                    "Please recommend 5 IB-style essay topics that combine academic rigor with the student's interests. " +
                    "Return ONLY a valid JSON object in this exact format: " +
                    "{\"ia_topics\": [{\"title\": \"...\", \"description\": \"...\"}]}. " +
                    "No extra text, just the JSON.";

            String response = openAiService.callAssistant(assistantKey, prompt);
            log.debug("Recommend topics response: {}", response);

            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> result = gson.fromJson(response, mapType);

            AiIARecommendation recommendation = AiIARecommendation.create(interest, subject, response, user);
            repository.save(recommendation);

            return result;
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to recommend topics", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> createTopicGuide(String subject, String interestTopic, Object topic, User user) {
        try {
            String prompt = "You are an IB academic writing coach. " +
                    "The student is studying: " + subject + ". " +
                    "Create a detailed writing guide for the following IB essay topic: " + topic.toString() + ". " +
                    "Interest area: " + interestTopic + ". " +
                    "Return ONLY a valid JSON object with: " +
                    "{\"guide\": {\"title\": \"...\", \"overview\": \"...\", \"researchQuestions\": [...], " +
                    "\"keyPoints\": [...], \"structure\": {\"introduction\": \"...\", \"body\": \"...\", " +
                    "\"conclusion\": \"...\"}, \"tips\": [...]}}. No extra text.";

            String response = openAiService.callAssistant(assistantKey, prompt);
            log.debug("Topic guide response: {}", response);

            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(response, mapType);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create topic guide", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, Object> englishChat(String subject, String prompt, String textType, String responseMode, User user) {
        try {
            String systemPrompt;
            if ("evaluative".equals(responseMode)) {
                systemPrompt = "You are an IB Language A English examiner. " +
                        "Evaluate the following student response for " + subject + ". " +
                        "Text type: " + textType + ". " +
                        "Provide detailed feedback on content, structure, language use, and suggest improvements. " +
                        "Be encouraging but specific.";
            } else {
                systemPrompt = "You are an IB Language A English writing tutor. " +
                        "Help the student with their writing for " + subject + ". " +
                        "Text type: " + textType + ". " +
                        "Generate a helpful, detailed response.";
            }

            String response = openAiService.chatCompletion(systemPrompt, prompt);
            log.debug("English chat response length: {}", response.length());

            Map<String, Object> result = new HashMap<>();
            result.put("reply", response);
            return result;
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process english chat", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

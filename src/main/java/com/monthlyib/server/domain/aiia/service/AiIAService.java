package com.monthlyib.server.domain.aiia.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aihistory.entity.AiToolActionType;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.aihistory.model.AiToolHistoryCreateCommand;
import com.monthlyib.server.domain.aihistory.service.AiToolHistoryService;
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
    private final AiToolHistoryService aiToolHistoryService;

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

            Map<String, Object> result = parseAssistantJson(response);

            AiIARecommendation recommendation = AiIARecommendation.create(interest, subject, response, user);
            AiIARecommendation savedRecommendation = repository.save(recommendation);

            aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                    .user(user)
                    .toolType(AiToolType.IA_COACHING)
                    .actionType(AiToolActionType.TOPIC_RECOMMEND)
                    .title("AI IA 주제 추천")
                    .summary(buildTopicRecommendSummary(result))
                    .subject(subject)
                    .interestTopic(interest)
                    .relatedEntityId(savedRecommendation.getId())
                    .requestPayload(buildRecommendRequest(subject, interest))
                    .responsePayload(result)
                    .build());

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
            String topicText = topic instanceof Map<?, ?> topicMap
                    ? gson.toJson(topicMap)
                    : String.valueOf(topic);

            String prompt = "You are an IB academic writing coach. " +
                    "The student is studying: " + subject + ". " +
                    "Create a detailed writing guide for the following IB essay topic: " + topicText + ". " +
                    "Interest area: " + interestTopic + ". " +
                    "Return ONLY a valid JSON object with: " +
                    "{\"guide\": {\"title\": \"...\", \"overview\": \"...\", \"researchQuestions\": [...], " +
                    "\"keyPoints\": [...], \"structure\": {\"introduction\": \"...\", \"body\": \"...\", " +
                    "\"conclusion\": \"...\"}, \"tips\": [...]}}. No extra text.";

            String response = openAiService.callAssistant(assistantKey, prompt);
            log.debug("Topic guide response: {}", response);
            Map<String, Object> result = parseAssistantJson(response);

            aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                    .user(user)
                    .toolType(AiToolType.IA_COACHING)
                    .actionType(AiToolActionType.TOPIC_GUIDE)
                    .title("AI IA 가이드 생성")
                    .summary(buildTopicGuideSummary(result, topic))
                    .subject(subject)
                    .interestTopic(interestTopic)
                    .requestPayload(buildTopicGuideRequest(subject, interestTopic, topic))
                    .responsePayload(result)
                    .build());

            return result;
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

            aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                    .user(user)
                    .toolType(AiToolType.IA_COACHING)
                    .actionType(AiToolActionType.ENGLISH_CHAT)
                    .title("AI 영어 코칭")
                    .summary(truncate(response, 180))
                    .subject(subject)
                    .requestPayload(buildEnglishChatRequest(subject, prompt, textType, responseMode))
                    .responsePayload(result)
                    .build());
            return result;
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process english chat", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> parseAssistantJson(String response) {
        try {
            String cleanedResponse = response == null ? "" : response.trim();

            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse
                        .replaceAll("^```json\\s*", "")
                        .replaceAll("^```\\s*", "")
                        .replaceAll("\\s*```$", "")
                        .trim();
            }

            JsonElement parsed = JsonParser.parseString(cleanedResponse);
            if (parsed.isJsonPrimitive() && parsed.getAsJsonPrimitive().isString()) {
                cleanedResponse = parsed.getAsString().trim();
                if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse
                            .replaceAll("^```json\\s*", "")
                            .replaceAll("^```\\s*", "")
                            .replaceAll("\\s*```$", "")
                            .trim();
                }
                parsed = JsonParser.parseString(cleanedResponse);
            }

            if (!parsed.isJsonObject()) {
                throw new IllegalStateException("Assistant response is not a JSON object");
            }

            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            return gson.fromJson(parsed.getAsJsonObject(), mapType);
        } catch (Exception e) {
            log.error("Failed to parse assistant JSON response: {}", response, e);
            throw e;
        }
    }

    private Map<String, Object> buildRecommendRequest(String subject, String interest) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("subject", subject);
        payload.put("interest", interest);
        return payload;
    }

    private Map<String, Object> buildTopicGuideRequest(String subject, String interestTopic, Object topic) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("subject", subject);
        payload.put("interestTopic", interestTopic);
        payload.put("topic", topic);
        return payload;
    }

    private Map<String, Object> buildEnglishChatRequest(String subject, String prompt, String textType, String responseMode) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("subject", subject);
        payload.put("prompt", prompt);
        payload.put("textType", textType);
        payload.put("responseMode", responseMode);
        return payload;
    }

    @SuppressWarnings("unchecked")
    private String buildTopicRecommendSummary(Map<String, Object> result) {
        Object topics = result.get("ia_topics");
        if (topics instanceof java.util.List<?> topicList && !topicList.isEmpty()) {
            Object first = topicList.get(0);
            if (first instanceof Map<?, ?> firstMap && firstMap.get("title") != null) {
                return truncate(String.valueOf(firstMap.get("title")), 180);
            }
        }
        return "추천 주제 생성 완료";
    }

    private String buildTopicGuideSummary(Map<String, Object> result, Object topic) {
        Object guide = result.get("guide");
        if (guide instanceof Map<?, ?> guideMap) {
            if (guideMap.get("title") != null) {
                return truncate(String.valueOf(guideMap.get("title")), 180);
            }
            if (guideMap.get("overview") != null) {
                return truncate(String.valueOf(guideMap.get("overview")), 180);
            }
        }
        if (topic instanceof Map<?, ?> topicMap && topicMap.get("title") != null) {
            return truncate(String.valueOf(topicMap.get("title")), 180);
        }
        return "가이드 생성 완료";
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 1)) + "…";
    }
}

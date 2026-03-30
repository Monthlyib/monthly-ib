package com.monthlyib.server.domain.aiia.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.ai.service.OpenAiClientService;
import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;
import com.monthlyib.server.domain.aiia.repository.AiIARecommendationRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiIAService {

    private final AiIARecommendationRepository aiIARecommendationRepository;
    private final OpenAiClientService openAiClientService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> recommendTopics(String subject, String interest, User user) {
        log.warn("[recommendTopics] subject={}, interest={}", subject, interest);

        AiIARecommendation recommendation = AiIARecommendation.builder()
                .subject(subject)
                .interest(interest)
                .user(user)
                .topics("") // initialize empty
                .build();
        aiIARecommendationRepository.save(recommendation);

        try {
            String systemPrompt = """
                    You are an IB IA topic recommendation coach.
                    Return valid JSON only with this structure:
                    {
                      "subject": string,
                      "interest_topic": string,
                      "ia_topics": [
                        {
                          "title": string,
                          "description": string,
                          "research_angle": string,
                          "possible_method": string,
                          "why_this_works": string
                        }
                      ]
                    }
                    Rules:
                    - Recommend exactly 3 strong topics.
                    - Keep each topic specific, feasible, and IB-appropriate.
                    - Do not output markdown.
                    """;

            String userPrompt = """
                    Subject: %s
                    Interest Topic: %s
                    Generate 3 IA topic recommendations.
                    """.formatted(subject, interest);

            JsonNode responseNode = openAiClientService.chatForJson(systemPrompt, userPrompt);
            Map<String, Object> result = normalizeRecommendedTopics(responseNode, subject, interest);

            recommendation.setTopics(objectMapper.writeValueAsString(result));
            aiIARecommendationRepository.save(recommendation);
            return result;
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("GPT 토픽 추천 API 오류", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "토픽 추천 실패");
        }
    }

    public Map<String, Object> createTopicGuide(String subject, String interestTopic, Map<String, Object> topic,
            User user) {
        log.warn("[generateTopicGuide] subject={}, interestTopic={}", subject, interestTopic);

        try {
            String selectedTopic = objectMapper.writeValueAsString(topic == null ? Map.of() : topic);
            String systemPrompt = """
                    You are an IB IA guide generator.
                    Return valid JSON only as a single object.
                    Build a practical guide for the selected IA topic.
                    The object should include:
                    - subject
                    - interest_topic
                    - selected_topic
                    - refined_research_question
                    - why_this_topic_works
                    - research_scope (array)
                    - method_outline (array of objects)
                    - sources_or_data_to_collect (array)
                    - draft_outline (array of objects)
                    - risks_and_limitations (array)
                    - next_actions (array)
                    Do not output markdown.
                    """;

            String userPrompt = """
                    Subject: %s
                    Interest Topic: %s
                    Selected Topic JSON: %s
                    Generate a concrete IA guide that the student can use immediately.
                    """.formatted(subject, interestTopic, selectedTopic);

            JsonNode responseNode = openAiClientService.chatForJson(systemPrompt, userPrompt);
            Map<String, Object> result = toMutableMap(responseNode);
            result.putIfAbsent("subject", subject);
            result.putIfAbsent("interest_topic", interestTopic);
            result.putIfAbsent("selected_topic", topic == null ? Map.of() : topic);
            return result;
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("GPT 토픽 가이드 생성 오류", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "토픽 가이드 생성 실패");
        }
    }

    public Map<String, Object> englishChat(
            String subject,
            String textType,
            String responseMode,
            String prompt,
            User user) {
        // 1) subject guard
        log.warn("[englishChat] textType={}, responseMode={}", textType, responseMode);
        if (!"Langauge A English".equals(subject)) {
            throw new IllegalArgumentException("subject must be 'Langauge A English'");
        }

        try {
            String systemPrompt = buildEnglishSystemPrompt(textType, responseMode);
            String userPrompt = """
                    Subject: %s
                    Text Type: %s
                    Response Mode: %s
                    User Prompt: %s
                    """.formatted(subject, textType, responseMode, prompt);

            JsonNode responseNode = openAiClientService.chatForJson(systemPrompt, userPrompt);
            return normalizeEnglishResponse(responseNode, subject, textType, responseMode, prompt);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("English chat assistant 호출 중 오류", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "English chat failed");
        }
    }

    private Map<String, Object> normalizeRecommendedTopics(JsonNode responseNode, String subject, String interest) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subject", subject);
        result.put("interest_topic", interest);

        JsonNode topicsNode = responseNode.path("ia_topics");
        if (!topicsNode.isArray() && responseNode.isArray()) {
            topicsNode = responseNode;
        }

        List<Map<String, Object>> topics = new ArrayList<>();
        if (topicsNode.isArray()) {
            for (JsonNode topicNode : topicsNode) {
                topics.add(normalizeTopic(topicNode));
            }
        }

        result.put("ia_topics", topics);
        return result;
    }

    private Map<String, Object> normalizeTopic(JsonNode topicNode) {
        Map<String, Object> topic = new LinkedHashMap<>();

        if (topicNode.isTextual()) {
            topic.put("title", topicNode.asText());
            topic.put("description", "");
            return topic;
        }

        topic.put("title", firstNonBlank(
                topicNode.path("title").asText(null),
                topicNode.path("question").asText(null),
                "추천 주제"));
        topic.put("description", firstNonBlank(topicNode.path("description").asText(null), ""));

        putIfPresent(topic, "research_angle", topicNode.path("research_angle").asText(null));
        putIfPresent(topic, "possible_method", topicNode.path("possible_method").asText(null));
        putIfPresent(topic, "why_this_works", topicNode.path("why_this_works").asText(null));
        return topic;
    }

    private Map<String, Object> normalizeEnglishResponse(
            JsonNode responseNode,
            String subject,
            String textType,
            String responseMode,
            String prompt) {
        Map<String, Object> result = toMutableMap(responseNode);

        if ("generative".equals(responseMode)) {
            result.put("response_mode", "generative");
            result.putIfAbsent("interest_topic", prompt);
            ensureList(result, "building_steps");
            ensureList(result, "core_concepts_with_questions");
            ensureList(result, "suggested_topics");
        } else {
            result.put("response_mode", "evaluative_feedback");
            result.putIfAbsent("student_question", prompt);
            ensureList(result, "building_steps");
            ensureList(result, "identified_limitations");
            ensureList(result, "suggested_revisions");
            ensureList(result, "core_concepts_with_questions");
        }

        result.put("meta", Map.of(
                "subject", subject,
                "textType", textType,
                "mode", responseMode));
        return result;
    }

    private String buildEnglishSystemPrompt(String textType, String responseMode) {
        String trackInstructions = "Literature".equals(textType)
                ? "Focus on literary works, authorial choices, narrative voice, form, and interpretation."
                : "Focus on language use, rhetorical choices, audience, context, and non-literary analysis.";

        if ("generative".equals(responseMode)) {
            return """
                    You are an IB English %s coach.
                    %s
                    Return valid JSON only with this exact structure:
                    {
                      "response_mode": "generative",
                      "interest_topic": string,
                      "intro_paragraph": string,
                      "building_steps": [
                        { "step_number": integer, "title": string, "instruction": string, "example": string }
                      ],
                      "core_concepts_with_questions": [
                        { "concept": string, "guiding_questions": [string] }
                      ],
                      "suggested_topics": [
                        { "question": string, "ib_core_concept": string, "description": string, "essay_guideline": string }
                      ]
                    }
                    Rules:
                    - Provide 3 to 5 building_steps.
                    - Provide 3 suggested_topics.
                    - Keep suggestions specific and IB-appropriate.
                    - Do not output markdown.
                    """.formatted(textType, trackInstructions);
        }

        return """
                You are an IB English %s evaluator.
                %s
                Return valid JSON only with this exact structure:
                {
                  "response_mode": "evaluative_feedback",
                  "student_question": string,
                  "evaluation_intro": string,
                  "building_steps": [
                    { "step_number": integer, "title": string, "instruction": string, "example": string }
                  ],
                  "identified_limitations": [
                    { "issue": string, "explanation": string }
                  ],
                  "suggested_revisions": [
                    { "question": string, "ib_core_concept": string, "description": string, "essay_guideline": string }
                  ],
                  "core_concepts_with_questions": [
                    { "concept": string, "guiding_questions": [string] }
                  ]
                }
                Rules:
                - Evaluate the student's prompt critically but constructively.
                - Provide 2 to 4 identified_limitations.
                - Provide 2 to 3 suggested_revisions.
                - Do not output markdown.
                """.formatted(textType, trackInstructions);
    }

    private Map<String, Object> toMutableMap(JsonNode node) {
        if (!node.isObject()) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", node.toString());
            return fallback;
        }

        return objectMapper.convertValue(node, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    private void ensureList(Map<String, Object> map, String key) {
        if (!(map.get(key) instanceof List<?>)) {
            map.put(key, List.of());
        }
    }

    private void putIfPresent(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}

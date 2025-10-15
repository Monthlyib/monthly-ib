package com.monthlyib.server.domain.aiia.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monthlyib.server.domain.aiia.entity.AiIARecommendation;
import com.monthlyib.server.domain.aiia.repository.AiIARecommendationRepository;
import com.monthlyib.server.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiIAService {

    private final AiIARecommendationRepository aiIARecommendationRepository;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> recommendTopics(String subject, String interest, User user) {
        Logger log = LoggerFactory.getLogger(AiIAService.class);
        log.warn("[recommendTopics] subject={}, interest={}", subject, interest);

        AiIARecommendation recommendation = AiIARecommendation.builder()
                .subject(subject)
                .interest(interest)
                .user(user)
                .topics("") // initialize empty
                .build();
        aiIARecommendationRepository.save(recommendation);

        try {
            Map<String, String> assistantMap = Map.of(
                    "Science", "asst_AOfc3JgUHv0TEAt1TW3BlrUj",
                    "Math", "asst_CTKqoWcoSQVxvGwvBEnLU1JK",
                    "Langauge A English", "asst_MYL7Zgg1btoI9oxfqJFrJNTr",
                    "Psychology", "asst_vR4kvb3si89JhVbStxx8zUy4",
                    "Business", "asst_8THzNZp7ajvDqzTWUV6xbwT3",
                    "History", "asst_2qQTQt9UMdA20f59rg7RtLt4",
                    "Geography", "asst_CedDfZnJ8xP0Z6LRPzTQryS9",
                    "Economics", "asst_FWEEdenoYroG02QPw8vZ0JnT");
            String assistantId = assistantMap.getOrDefault(subject, "asst_default_id").trim();

            log.warn("Using assistant ID: " + assistantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("OpenAI-Beta", "assistants=v2");

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            // Create thread
            ResponseEntity<JsonNode> threadResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads",
                    HttpMethod.POST,
                    new HttpEntity<>("{}", headers),
                    JsonNode.class);
            String threadId = threadResponse.getBody().path("id").asText();

            // Add message
            ObjectNode messageBody = objectMapper.createObjectNode();
            messageBody.put("role", "user");

            ObjectNode contentText = objectMapper.createObjectNode();
            contentText.put("type", "text");
            contentText.put("text", String.format(
                    "Subject: %s\nInterest: %s\nPlease recommend 3 suitable IA topics.", subject,
                    interest));
            messageBody.set("content", objectMapper.createArrayNode().add(contentText));

            HttpEntity<String> addMessageEntity = new HttpEntity<>(messageBody.toString(), headers);
            restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.POST,
                    addMessageEntity,
                    String.class);

            // Run assistant
            ObjectNode runRequest = objectMapper.createObjectNode();
            runRequest.put("assistant_id", assistantId);
            HttpEntity<String> runEntity = new HttpEntity<>(runRequest.toString(), headers);
            ResponseEntity<JsonNode> runResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/runs",
                    HttpMethod.POST,
                    runEntity,
                    JsonNode.class);
            String runId = runResponse.getBody().path("id").asText();

            // Wait for run to complete
            String status;
            do {
                Thread.sleep(1000);
                ResponseEntity<JsonNode> statusResponse = restTemplate.exchange(
                        "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        JsonNode.class);
                status = statusResponse.getBody().path("status").asText();
            } while (!"completed".equals(status));

            // Fetch messages
            ResponseEntity<JsonNode> messagesResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class);
            String content = messagesResponse.getBody()
                    .path("data").get(0)
                    .path("content").get(0)
                    .path("text").path("value").asText();

            recommendation.setTopics(content);
            aiIARecommendationRepository.save(recommendation);

            // Attempt to extract a single JSON object from the assistant response and
            // forward as-is
            String jsonCandidate = extractJsonBlock(content);

            if (jsonCandidate != null) {
                try {
                    // Try parsing as JSON
                    JsonNode node = objectMapper.readTree(jsonCandidate);
                    // Return as a Map so the controller serializes it intact
                    return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception parseEx) {
                    // Clean up trailing commas and retry once
                    String cleaned = cleanupLooseCommas(jsonCandidate);
                    try {
                        JsonNode node = objectMapper.readTree(cleaned);
                        return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
                        });
                    } catch (Exception ignore) {
                        // fall through to raw content
                    }
                }
            }

            // Fallback: return the raw content as a map
            return Map.of("raw", content);

        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AiIAService.class);
            logger.error("GPT 토픽 추천 API 오류", e);
            throw new RuntimeException("토픽 추천 실패", e);
        }
    }

    public Map<String, Object> createTopicGuide(String subject, String interestTopic, Map<String, Object> topic,
            User user) {
        Logger log = LoggerFactory.getLogger(AiIAService.class);
        log.warn("[generateTopicGuide] subject={}, interestTopic={}", subject, interestTopic);

        try {
            // 과목별 가이드 Assistant 매핑 (필요 시 실제 ID로 교체)
            Map<String, String> assistantMap = Map.of(
                    "Science", "asst_AOfc3JgUHv0TEAt1TW3BlrUj",
                    "Math", "asst_CTKqoWcoSQVxvGwvBEnLU1JK",
                    "Langauge A English", "asst_MYL7Zgg1btoI9oxfqJFrJNTr",
                    "Psychology", "asst_vR4kvb3si89JhVbStxx8zUy4",
                    "Business", "asst_a6X89XM9zzYupzETbTq5wHCG",
                    "History", "asst_2qQTQt9UMdA20f59rg7RtLt4",
                    "Geography", "asst_CedDfZnJ8xP0Z6LRPzTQryS9",
                    "Economics", "asst_FWEEdenoYroG02QPw8vZ0JnT");
            String assistantId = assistantMap.getOrDefault(subject, "asst_default_id").trim();
            log.warn("[generateTopicGuide] Using guide assistant ID: {}", assistantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("OpenAI-Beta", "assistants=v2");

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            // 1) Thread 생성
            ResponseEntity<JsonNode> threadResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads",
                    HttpMethod.POST,
                    new HttpEntity<>("{}", headers),
                    JsonNode.class);
            String threadId = threadResponse.getBody().path("id").asText();

            // Build assistant input JSON per required schema:
            // {
            // "Subject": "...",
            // "Interest Topic": "...",
            // "ia_topics": [ "..." ]
            // }
            //
            // Prefer a string title if available; otherwise fallback to a compact string of
            // the topic map
            String topicTitle = null;
            if (topic != null) {
                Object t = topic.get("title");
                if (t != null) {
                    topicTitle = String.valueOf(t);
                }
            }
            if (topicTitle == null) {
                // Fallback: try description, else toString()
                Object d = (topic != null) ? topic.get("description") : null;
                topicTitle = (d != null) ? String.valueOf(d) : String.valueOf(topic);
            }

            ObjectNode inputJson = objectMapper.createObjectNode();
            inputJson.put("Subject", subject);
            inputJson.put("Interest Topic", interestTopic);

            // ia_topics should be an array of strings. We pass the selected one.
            var iaTopicsArray = objectMapper.createArrayNode();
            if (topicTitle != null) {
                iaTopicsArray.add(topicTitle);
            }
            inputJson.set("ia_topics", iaTopicsArray);

            String payloadJson = objectMapper.writeValueAsString(inputJson);
            log.warn("[generateTopicGuide] Sending payload to assistant: {}", payloadJson);

            ObjectNode messageBody = objectMapper.createObjectNode();
            messageBody.put("role", "user");
            ObjectNode contentText = objectMapper.createObjectNode();
            contentText.put("type", "text");
            contentText.put("text", payloadJson);
            messageBody.set("content", objectMapper.createArrayNode().add(contentText));

            restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.POST,
                    new HttpEntity<>(messageBody.toString(), headers),
                    String.class);

            // 3) Assistant Run 실행
            ObjectNode runRequest = objectMapper.createObjectNode();
            runRequest.put("assistant_id", assistantId);
            ResponseEntity<JsonNode> runResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/runs",
                    HttpMethod.POST,
                    new HttpEntity<>(runRequest.toString(), headers),
                    JsonNode.class);
            String runId = runResponse.getBody().path("id").asText();

            // 4) 완료 대기
            String status;
            do {
                Thread.sleep(1000);
                ResponseEntity<JsonNode> statusResponse = restTemplate.exchange(
                        "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        JsonNode.class);
                status = statusResponse.getBody().path("status").asText();
            } while (!"completed".equals(status));

            // 5) 메시지 조회 & JSON 파싱
            ResponseEntity<JsonNode> messagesResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class);
            String content = messagesResponse.getBody()
                    .path("data").get(0)
                    .path("content").get(0)
                    .path("text").path("value").asText();

            // JSON 블록 추출 후 파싱 시도
            String jsonCandidate = extractJsonBlock(content);
            ObjectMapper om = objectMapper;
            if (jsonCandidate != null) {
                try {
                    JsonNode node = om.readTree(jsonCandidate);
                    return om.convertValue(node, new TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception parseEx) {
                    String cleaned = cleanupLooseCommas(jsonCandidate);
                    try {
                        JsonNode node = om.readTree(cleaned);
                        return om.convertValue(node, new TypeReference<Map<String, Object>>() {
                        });
                    } catch (Exception ignore) {
                        // 아래 fallback으로 이동
                    }
                }
            }

            // Fallback: 원문 전달
            return Map.of("raw", content);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AiIAService.class);
            logger.error("GPT 토픽 가이드 생성 오류", e);
            throw new RuntimeException("토픽 가이드 생성 실패", e);
        }
    }

    /**
     * Extracts the first JSON object block from a text blob by taking the substring
     * between the first '{' and the last '}'.
     */
    private String extractJsonBlock(String text) {
        if (text == null)
            return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    /**
     * Cleans common formatting artifacts such as trailing commas before '}' or ']'
     * and duplicate commas that can appear in LLM outputs.
     */
    private String cleanupLooseCommas(String json) {
        if (json == null)
            return null;
        // Remove a comma that is immediately before a closing brace/bracket
        String cleaned = json.replaceAll(",\\s*([}\\]])", "$1");
        // Collapse duplicate commas
        cleaned = cleaned.replaceAll("\\s*,\\s*,", ",");
        return cleaned;
    }

    public Map<String, Object> englishChat(
            String subject,
            String textType,
            String responseMode,
            String prompt,
            User user) {
        Logger log = LoggerFactory.getLogger(AiIAService.class);
        // 1) subject guard
        log.warn("textType",textType);
        if (!"Langauge A English".equals(subject)) {
            throw new IllegalArgumentException("subject must be 'Langauge A English'");
        }

        // 2) assistant 라우팅 (textType x responseMode 조합)
        // 예시는 placeholder ID — 실제 발급된 Assistant ID로 교체
        Map<String, String> assistantMap = Map.of(
                "Language|generative", "asst_mPF4fipHJC9ZMp9SnROruory",
                "Language|evaluate", "asst_w9Mmr2w7NGeL9xkNYwU1VUG0",
                "Literature|generative", "asst_Ao5PhFjRQjTiPOFz0e1KNoFx",
                "Literature|evaluate", "asst_NJfjMrqZq0WaYOnLWaqV6sXF");

        String key = textType + "|" + responseMode;
        log.warn("[generateTopicGuide] Sending payload to assistant: {}", key);

        String assistantId = assistantMap.getOrDefault(key, "asst_default_id");

        // 3) OpenAI Assistants v2 호출 (기존 generateAnswerFeedback 로직 패턴 재활용)
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("OpenAI-Beta", "assistants=v2");
            RestTemplate restTemplate = new RestTemplate();

            // thread 생성
            ResponseEntity<JsonNode> threadResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads",
                    HttpMethod.POST,
                    new HttpEntity<>("{}", headers),
                    JsonNode.class);
            String threadId = threadResponse.getBody().path("id").asText();

            // message 추가: 요구 포맷(백엔드에서 모든 라우팅 정보를 포함)
            // 프론트에서 전달 받은 선택사항을 명시적으로 넣어줌
            ObjectNode messageBody = objectMapper.createObjectNode();
            messageBody.put("role", "user");

            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");

            // 어시스턴트에 전달할 입력 형식은 English 전용 채팅 스펙에 맞춰 자유롭게 구성
            // 필요하다면 JSON 형식 문자열로 명확히 전달
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("Subject", subject);
            payload.put("TextType", textType);
            payload.put("Mode", responseMode);
            payload.put("Prompt", prompt);

            textContent.put("text", payload.toString());
            messageBody.set("content", objectMapper.createArrayNode().add(textContent));

            restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.POST,
                    new HttpEntity<>(messageBody.toString(), headers),
                    String.class);

            // run
            ObjectNode runRequest = objectMapper.createObjectNode();
            runRequest.put("assistant_id", assistantId);
            ResponseEntity<JsonNode> runResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/runs",
                    HttpMethod.POST,
                    new HttpEntity<>(runRequest.toString(), headers),
                    JsonNode.class);
            String runId = runResponse.getBody().path("id").asText();

            // 완료 대기 (polling)
            String status;
            do {
                Thread.sleep(1000);
                ResponseEntity<JsonNode> statusResponse = restTemplate.exchange(
                        "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        JsonNode.class);
                status = statusResponse.getBody().path("status").asText();
                if ("failed".equals(status)) {
                    throw new RuntimeException("assistant run failed");
                }
            } while (!"completed".equals(status));

            // 메시지 조회
            ResponseEntity<JsonNode> messagesResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/messages",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    JsonNode.class);

            // 최신 메시지 텍스트 추출
            JsonNode data = messagesResponse.getBody().path("data");
            String value = data.get(0).path("content").get(0).path("text").path("value").asText();

            // value 가 JSON 문자열이면 파싱, 아니면 그대로 감싸 반환
            Map<String, Object> resultMap = new HashMap<>();
            try {
                JsonNode parsed = objectMapper.readTree(value);
                resultMap = objectMapper.convertValue(parsed, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception ignore) {
                resultMap.put("text", value);
            }

            // 메타 정보 추가(선택)
            resultMap.put("meta", Map.of(
                    "subject", subject,
                    "textType", textType,
                    "mode", responseMode));

            return resultMap;

        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AiIAService.class);
            logger.error("English chat assistant 호출 중 오류", e);
            throw new RuntimeException("English chat failed", e);
        }
    }

}

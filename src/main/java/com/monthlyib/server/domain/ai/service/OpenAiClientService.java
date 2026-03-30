package com.monthlyib.server.domain.ai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiClientService {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String AUDIO_TRANSCRIPTIONS_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final String DEFAULT_CHAT_MODEL = "gpt-4o";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    public String chat(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, objectMapper.getNodeFactory().textNode(userPrompt), false);
    }

    public String chat(String systemPrompt, JsonNode userContent) {
        return chat(systemPrompt, userContent, false);
    }

    public JsonNode chatForJson(String systemPrompt, String userPrompt) {
        return parseJson(chat(systemPrompt, objectMapper.getNodeFactory().textNode(userPrompt), true));
    }

    public JsonNode chatForJson(String systemPrompt, JsonNode userContent) {
        return parseJson(chat(systemPrompt, userContent, true));
    }

    public ArrayNode createUserContent(String text, String imageUrl) {
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.put("type", "text");
        textNode.put("text", text);
        content.add(textNode);

        if (imageUrl != null && !imageUrl.isBlank()) {
            ObjectNode imageNode = objectMapper.createObjectNode();
            imageNode.put("type", "image_url");

            ObjectNode imageUrlNode = objectMapper.createObjectNode();
            imageUrlNode.put("url", imageUrl);
            imageNode.set("image_url", imageUrlNode);
            content.add(imageNode);
        }

        return content;
    }

    public String transcribe(
            MultipartFile audioFile,
            String model,
            String responseFormat,
            List<String> includes,
            List<String> timestampGranularities) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(openAiApiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename() != null ? audioFile.getOriginalFilename() : "input.webm";
                }
            };

            body.add("file", fileResource);
            body.add("model", model);
            body.add("response_format", responseFormat);
            appendValues(body, "include[]", includes);
            appendValues(body, "timestamp_granularities[]", timestampGranularities);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    AUDIO_TRANSCRIPTIONS_URL,
                    requestEntity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            throw openAiFailure("audio transcription", e);
        }
    }

    private String chat(String systemPrompt, JsonNode userContent, boolean expectJson) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", DEFAULT_CHAT_MODEL);
            requestBody.put("temperature", expectJson ? 0.2 : 0.4);

            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(createMessage("system", objectMapper.getNodeFactory().textNode(systemPrompt)));
            messages.add(createMessage("user", userContent));
            requestBody.set("messages", messages);

            if (expectJson) {
                ObjectNode responseFormat = objectMapper.createObjectNode();
                responseFormat.put("type", "json_object");
                requestBody.set("response_format", responseFormat);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    CHAT_COMPLETIONS_URL,
                    requestEntity,
                    String.class);

            JsonNode responseNode = objectMapper.readTree(response.getBody());
            return extractMessageContent(responseNode);
        } catch (Exception e) {
            throw openAiFailure(expectJson ? "structured chat completion" : "chat completion", e);
        }
    }

    private ObjectNode createMessage(String role, JsonNode content) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        if (content.isTextual()) {
            message.put("content", content.asText());
        } else {
            message.set("content", content);
        }
        return message;
    }

    private String extractMessageContent(JsonNode responseNode) {
        JsonNode choicesNode = responseNode.path("choices");
        if (!choicesNode.isArray() || choicesNode.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "OpenAI 응답에 choices가 없습니다.");
        }

        JsonNode contentNode = choicesNode.get(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                if (item.has("text")) {
                    builder.append(item.path("text").asText());
                }
            }
            String content = builder.toString().trim();
            if (!content.isEmpty()) {
                return content;
            }
        }

        throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "OpenAI 응답 본문을 해석하지 못했습니다.");
    }

    private JsonNode parseJson(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception firstException) {
            String jsonCandidate = extractJsonBlock(content);
            if (jsonCandidate == null) {
                throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "OpenAI JSON 응답을 해석하지 못했습니다.");
            }

            try {
                return objectMapper.readTree(cleanupLooseCommas(jsonCandidate));
            } catch (Exception secondException) {
                throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "OpenAI JSON 응답을 해석하지 못했습니다.");
            }
        }
    }

    private String extractJsonBlock(String text) {
        if (text == null) {
            return null;
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private String cleanupLooseCommas(String json) {
        String cleaned = json.replaceAll(",\\s*([}\\]])", "$1");
        return cleaned.replaceAll("\\s*,\\s*,", ",");
    }

    private void appendValues(MultiValueMap<String, Object> body, String key, List<String> values) {
        if (values == null) {
            return;
        }

        for (String value : values) {
            body.add(key, value);
        }
    }

    private ServiceLogicException openAiFailure(String operation, Exception exception) {
        if (exception instanceof ServiceLogicException serviceLogicException) {
            return serviceLogicException;
        }

        if (exception instanceof RestClientResponseException responseException) {
            String responseBody = responseException.getResponseBodyAsString();
            String detail = extractOpenAiErrorMessage(responseBody);
            log.error(
                    "OpenAI {} failed. status={}, body={}",
                    operation,
                    responseException.getRawStatusCode(),
                    responseBody);
            return new ServiceLogicException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "OpenAI " + operation + " 호출 실패: " + sanitize(detail));
        }

        log.error("OpenAI {} failed", operation, exception);
        return new ServiceLogicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "OpenAI " + operation + " 호출 실패: " + sanitize(exception.getMessage()));
    }

    private String extractOpenAiErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText();
            if (message != null && !message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
        }

        return responseBody;
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "알 수 없는 오류";
        }

        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
    }
}

package com.monthlyib.server.domain.aiia.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class OpenAiAssistantService {

    private static final String BASE_URL = "https://api.openai.com";

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    private final Gson gson = new Gson();

    public String callAssistant(String assistantId, String prompt) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // Step 1: Create thread
            String threadId = createThread(httpClient);
            log.debug("Created thread: {}", threadId);

            // Step 2: Add message
            addMessage(httpClient, threadId, prompt);
            log.debug("Added message to thread: {}", threadId);

            // Step 3: Create run
            String runId = createRun(httpClient, threadId, assistantId);
            log.debug("Created run: {}", runId);

            // Step 4: Poll for completion
            pollForCompletion(httpClient, threadId, runId);
            log.debug("Run completed for thread: {}", threadId);

            // Step 5: Get messages
            String result = getLastAssistantMessage(httpClient, threadId);
            log.debug("Retrieved assistant message");
            return result;
        }
    }

    private String createThread(CloseableHttpClient httpClient) throws Exception {
        HttpPost post = new HttpPost(BASE_URL + "/v1/threads");
        applyHeaders(post);
        post.setEntity(new StringEntity("{}", StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.debug("Create thread response: {}", responseBody);
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.get("id").getAsString();
        }
    }

    private void addMessage(CloseableHttpClient httpClient, String threadId, String prompt) throws Exception {
        HttpPost post = new HttpPost(BASE_URL + "/v1/threads/" + threadId + "/messages");
        applyHeaders(post);

        JsonObject body = new JsonObject();
        body.addProperty("role", "user");
        body.addProperty("content", prompt);
        post.setEntity(new StringEntity(gson.toJson(body), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.debug("Add message response: {}", responseBody);
        }
    }

    private String createRun(CloseableHttpClient httpClient, String threadId, String assistantId) throws Exception {
        HttpPost post = new HttpPost(BASE_URL + "/v1/threads/" + threadId + "/runs");
        applyHeaders(post);

        JsonObject body = new JsonObject();
        body.addProperty("assistant_id", assistantId);
        post.setEntity(new StringEntity(gson.toJson(body), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.debug("Create run response: {}", responseBody);
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.get("id").getAsString();
        }
    }

    private void pollForCompletion(CloseableHttpClient httpClient, String threadId, String runId) throws Exception {
        int maxPolls = 30;
        for (int i = 0; i < maxPolls; i++) {
            Thread.sleep(1000);
            HttpGet get = new HttpGet(BASE_URL + "/v1/threads/" + threadId + "/runs/" + runId);
            applyGetHeaders(get);

            try (CloseableHttpResponse response = httpClient.execute(get)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                String status = json.get("status").getAsString();
                log.debug("Run status (poll {}): {}", i + 1, status);

                if ("completed".equals(status)) {
                    return;
                } else if ("failed".equals(status) || "cancelled".equals(status) || "expired".equals(status)) {
                    throw new RuntimeException("Run ended with status: " + status);
                }
            }
        }
        throw new RuntimeException("Run did not complete within maximum poll attempts");
    }

    private String getLastAssistantMessage(CloseableHttpClient httpClient, String threadId) throws Exception {
        HttpGet get = new HttpGet(BASE_URL + "/v1/threads/" + threadId + "/messages");
        applyGetHeaders(get);

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.debug("Messages response: {}", responseBody);
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");
            // Messages are ordered newest first; data[0] is the latest assistant message
            JsonObject firstMessage = data.get(0).getAsJsonObject();
            JsonArray content = firstMessage.getAsJsonArray("content");
            JsonObject textContent = content.get(0).getAsJsonObject();
            return textContent.getAsJsonObject("text").get("value").getAsString();
        }
    }

    public String chatCompletion(String systemPrompt, String userPrompt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(BASE_URL + "/v1/chat/completions");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            JsonObject body = new JsonObject();
            body.addProperty("model", "gpt-4o");
            body.addProperty("temperature", 0.7);

            JsonArray messages = new JsonArray();

            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", userPrompt);
            messages.add(userMessage);

            body.add("messages", messages);

            post.setEntity(new StringEntity(gson.toJson(body), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.debug("Chat completion response: {}", responseBody);
                JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                JsonArray choices = json.getAsJsonArray("choices");
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                return firstChoice.getAsJsonObject("message").get("content").getAsString();
            }
        } catch (Exception e) {
            log.error("Chat completion failed", e);
            throw new RuntimeException("Chat completion failed: " + e.getMessage(), e);
        }
    }

    private void applyHeaders(HttpPost post) {
        post.setHeader("Authorization", "Bearer " + apiKey);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("OpenAI-Beta", "assistants=v2");
    }

    private void applyGetHeaders(HttpGet get) {
        get.setHeader("Authorization", "Bearer " + apiKey);
        get.setHeader("Content-Type", "application/json");
        get.setHeader("OpenAI-Beta", "assistants=v2");
    }
}

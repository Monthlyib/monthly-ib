package com.monthlyib.server.domain.aiio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.repository.VoiceFeedbackRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AiioService {

    private final VoiceFeedbackRepository voiceFeedbackRepository;
    private final FileService fileService;
    // ObjectMapper for JSON parsing
    private final ObjectMapper objectMapper;

    // OpenAI API URL and API key (실제 서비스에서는 환경변수나 설정 파일을 통해 관리)
    private final String openaiUrl = "https://api.openai.com/v1/chat/completions";

    @Value("${OPENAI_API_KEY}")
    private String openaiApiKey;

    /**
     * 프론트엔드에서 전달받은 AiioPostDto와 파일(대본, 녹음), 사용자 정보를 이용하여
     * 파일 업로드 및 ChatGPT API 호출 후 VoiceFeedback 엔티티를 생성, 저장합니다.
     */
    public VoiceFeedback createFeedback(AiioPostDto postDto, MultipartFile scriptFile, MultipartFile audioFile, User user) {
        try {
            // 1. 유니크한 파일 경로 생성 (iocTopic, workTitle, 타임스탬프 조합)
            String timestamp = String.valueOf(System.currentTimeMillis());
            // 예: aiio/script/{iocTopic}_{workTitle}_{timestamp}/
            String uniqueScriptPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";
            String uniqueAudioPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";

            // 2. 파일 업로드: fileService.saveMultipartFileForAws(file, AwsProperty, path)
            String scriptFilePath = fileService.saveMultipartFileForAws(scriptFile, AwsProperty.AIIO_SCRIPT, uniqueScriptPath);
            String audioFilePath = fileService.saveMultipartFileForAws(audioFile, AwsProperty.AIIO_AUDIO, uniqueAudioPath);

            // 3. ChatGPT API 호출: 대본과 녹음 파일을 비교하여 피드백 생성
            // (실제 음성 처리 대신, 여기서는 대본 파일의 내용을 이용하여 피드백을 생성하는 예시입니다.)
            String scriptContent = new String(scriptFile.getBytes(), StandardCharsets.UTF_8);
            // 실제 서비스에서는 녹음 파일을 음성인식 서비스로 텍스트 변환한 후 비교할 수 있습니다.
            String audioTranscript = "Audio transcript placeholder: " + scriptContent; // 예시로 대본 내용을 활용

            String prompt = "Please analyze the following script and audio transcript, "
                    + "and provide detailed feedback on the user's tone, intonation, and overall speaking ability.\n\n"
                    + "Script: " + scriptContent + "\n\n"
                    + "Audio Transcript: " + audioTranscript;

            String escapedPrompt = escapeJson(prompt);
            String requestJson = "{\n" +
                    "  \"model\": \"gpt-4\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"system\", \"content\": \"You are an expert speech coach.\"},\n" +
                    "    {\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}\n" +
                    "  ]\n" +
                    "}";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> apiResponse = restTemplate.postForEntity(openaiUrl, requestEntity, String.class);
            String feedbackContent = extractFeedbackFromResponse(apiResponse.getBody());

            // 4. VoiceFeedback 엔티티 생성 (사용자 정보 포함)
            VoiceFeedback voiceFeedback = VoiceFeedback.builder()
                    .iocTopic(postDto.getIocTopic())
                    .workTitle(postDto.getWorkTitle())
                    .author(postDto.getAuthor())
                    .authorId(user.getUserId()) // User 엔티티에 userId 필드가 있다고 가정합니다.
                    .scriptFilePath(scriptFilePath)
                    .audioFilePath(audioFilePath)
                    .feedbackContent(feedbackContent)
                    .build();

            // 5. 엔티티 저장 후 반환
            return voiceFeedbackRepository.saveFeedback(voiceFeedback);
        } catch (Exception e) {
            log.error("Error in createFeedback: ", e);
            throw new ServiceLogicException(ErrorCode.NOT_FOUND); // 적절한 ErrorCode 사용
        }
    }

    /**
     * 피드백 수정 요청을 처리합니다.
     * 사용자 정보를 기준으로 기존 피드백을 조회하여 업데이트합니다.
     */
    public VoiceFeedback updateFeedback(AiioPatchDto patchDto, User user) {
        // 예시: 사용자 ID로 기존 피드백을 조회 (해당 메서드가 VoiceFeedbackRepository에 정의되어 있다고 가정)
        VoiceFeedback voiceFeedback = voiceFeedbackRepository.findFeedbackByAuthorId(user.getUserId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        // 피드백 내용 수정
        voiceFeedback.update(patchDto);
        return voiceFeedbackRepository.saveFeedback(voiceFeedback);
    }

    /**
     * 간단한 JSON 이스케이프 처리
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"");
    }

    /**
     * ChatGPT API의 응답에서 피드백 텍스트를 추출합니다.
     * 실제 구현에서는 Jackson 등의 JSON 라이브러리를 사용하여 파싱해야 합니다.
     */
    private String extractFeedbackFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // OpenAI의 응답 구조: choices[0].message.content 에 피드백이 존재합니다.
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error parsing ChatGPT API response", e);
            return "피드백 생성 중 오류가 발생했습니다.";
        }
    }
}
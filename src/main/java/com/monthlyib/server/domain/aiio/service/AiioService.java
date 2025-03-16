package com.monthlyib.server.domain.aiio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AiioService {

    private final VoiceFeedbackRepository voiceFeedbackRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    // OpenAI API URL and API key are managed via environment variables
    private final String openaiUrl = "https://api.openai.com/v1/chat/completions";

    @Value("${OPENAI_API_KEY}")
    private String openaiApiKey;

    /**
     * 프론트엔드에서 전달받은 AiioPostDto (텍스트 필드 및 첨부 파일)와 사용자 정보를 이용하여
     * 파일 업로드, Whisper API 호출을 통한 음성 전사, 자체 분석 결과를 포함한 ChatGPT API 호출 후
     * VoiceFeedback 엔티티를 생성, 저장합니다.
     */
    public VoiceFeedback createFeedback(AiioPostDto postDto, User user) {
        try {
            // 0. 사용자 정보 검증
            if (user == null || user.getUserId() == null) {
                log.error("User information is missing: {}", user);
                throw new ServiceLogicException(ErrorCode.NOT_FOUND, "User not authenticated or missing userId");
            }

            // DTO에서 파일 데이터를 추출
            MultipartFile scriptFile = postDto.getScriptFile();
            MultipartFile audioFile = postDto.getAudioFile();

            log.warn("scriptFile: {}", audioFile.getContentType());

            // 1. 유니크한 파일 경로 생성 (iocTopic, workTitle, 타임스탬프 조합)
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uniqueScriptPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";
            String uniqueAudioPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";

            // 2. 파일 업로드: 저장 후 반환된 URL 또는 경로 사용
            String scriptFilePath = fileService.saveMultipartFileForAws(scriptFile, AwsProperty.AIIO_SCRIPT, uniqueScriptPath);
            String audioFilePath = fileService.saveMultipartFileForAws(audioFile, AwsProperty.AIIO_AUDIO, uniqueAudioPath);

            // 3. Whisper API 호출: audioFile을 전송하여 음성 전사 결과(whisperTranscript)를 얻음.
            String whisperTranscript = callWhisperAPI(audioFile);

            // 4. 자체 분석 수행: Whisper API 응답을 기반으로 분석 결과를 산출 (여기서는 시뮬레이션)
            AnalysisResult analysisResult = analyzeWhisperResponse(whisperTranscript);

            // 5. 최종 프롬프트 구성: 점수 정보와 첨부 파일 관련 정보를 포함
            String prompt = generatePrompt(postDto, analysisResult, whisperTranscript, scriptFile);

            // 6. ChatGPT API 호출: ObjectMapper를 이용해 JSON 페이로드 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-4o-mini-2024-07-18");
            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an expert speech coach.");
            messages.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            requestBody.set("messages", messages);
            // 대본 파일 첨부: 만약 텍스트 파일이 아니라면 base64 인코딩하여 첨부 (텍스트 파일은 이미 프롬프트에 포함됨)
            if (!(scriptFile.getContentType() != null && scriptFile.getContentType().equals("text/plain"))) {
                String scriptFileBase64 = Base64.getEncoder().encodeToString(scriptFile.getBytes());
                requestBody.put("script_file_base64", scriptFileBase64);
            }
            // audioFile은 Whisper API로 처리되었으므로 첨부하지 않음

            String requestJson = objectMapper.writeValueAsString(requestBody);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(openaiUrl, requestEntity, String.class);
            String feedbackContent = extractFeedbackFromResponse(apiResponse.getBody());

            // 7. VoiceFeedback 엔티티 생성 및 저장
            VoiceFeedback voiceFeedback = VoiceFeedback.builder()
                    .iocTopic(postDto.getIocTopic())
                    .workTitle(postDto.getWorkTitle())
                    .author(postDto.getAuthor())
                    .authorId(user.getUserId())
                    .scriptFilePath(scriptFilePath)
                    .audioFilePath(audioFilePath)
                    .feedbackContent(feedbackContent)
                    .build();

            return voiceFeedbackRepository.saveFeedback(voiceFeedback);
        } catch (Exception e) {
            log.error("Error in createFeedback: ", e);
            throw new ServiceLogicException(ErrorCode.NOT_FOUND);
        }
    }

    /**
     * 피드백 수정 요청을 처리합니다.
     * 사용자 정보를 기준으로 기존 피드백을 조회하여 업데이트합니다.
     */
    public VoiceFeedback updateFeedback(AiioPatchDto patchDto, User user) {
        VoiceFeedback voiceFeedback = voiceFeedbackRepository.findFeedbackByAuthorId(user.getUserId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        voiceFeedback.update(patchDto);
        return voiceFeedbackRepository.saveFeedback(voiceFeedback);
    }

    /**
     * Whisper API를 호출하여 audioFile의 전사 결과를 반환합니다.

     */
    private String callWhisperAPI(MultipartFile audioFile) throws Exception {
        String whisperUrl = "https://api.openai.com/v1/audio/transcriptions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openaiApiKey);
    
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileAsResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                // 파일 형식을 명시적으로 "input.webm"으로 지정
                return "input.webm";
            }
        };
    
        log.warn("audioFile: {}", audioFile.getContentType());
        body.add("file", fileAsResource);
        body.add("model", "whisper-1");
        body.add("response_format", "json");
        // 필요 시 언어 설정 추가 (예: body.add("language", "ko"));
    
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(whisperUrl, requestEntity, String.class);
    
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("text").asText();
    }

    /**
     * Whisper API의 전사 결과를 분석하여 음성 발표의 여러 지표를 산출합니다.
     */
    private AnalysisResult analyzeWhisperResponse(String whisperTranscript) throws Exception {
        // 여기는 실제 Whisper 응답 분석 로직을 구현해야 합니다.
        // 예제에서는 간단히 두 문장의 confidence 평균, 속도, 긴장도(임의의 값)를 계산합니다.
        // whisperTranscript에 "sentences" 배열이 있다고 가정하고, 그 안에 confidence, start, end 값이 포함되어 있다고 가정합니다.
        // 실제 Whisper API 응답 구조에 맞게 수정해야 합니다.
        double totalConfidence = 0.0;
        int count = 0;
        double totalDuration = 0.0;
        double previousDuration = -1;
        double maxSpeedVariation = 0.0;

        // 예시: 단순히 텍스트 전체 길이 기반 계산 (실제 로직 필요)
        count = 1;
        totalConfidence = 90.0; // 예시 값
        totalDuration = 10.0;   // 예시 값
        previousDuration = 10.0;
        maxSpeedVariation = 2.0; // 예시 값

        int avgConfidence = count > 0 ? (int) ((totalConfidence / count)) : 0;
        int speechRateScore = avgConfidence; // 임의로 사용
        int tensionScore = (int) (maxSpeedVariation * 10);

        AnalysisResult result = new AnalysisResult();
        result.setPronunciationScore(avgConfidence);
        result.setSpeechRateScore(speechRateScore);
        result.setTensionScore(tensionScore);
        result.setUnclearSentences("Example unclear sentence."); // 예시
        return result;
    }

    /**
     * Analysis 결과와 Whisper 전사 결과, 그리고 대본 파일 정보를 바탕으로 최종 프롬프트를 생성합니다.
     */
    private String generatePrompt(AiioPostDto dto, AnalysisResult analysisResult, String whisperTranscript, MultipartFile scriptFile) throws Exception {
        // 점수 표시 (기존 방식 유지)
        String scoreInfo = "전체 발표 점수는 다음과 같습니다:\n"
                + "발음 명확도: " + analysisResult.getPronunciationScore() + "점, "
                + "발표 속도: " + analysisResult.getSpeechRateScore() + "점, "
                + "긴장도(속도 변화): " + analysisResult.getTensionScore() + "점입니다.\n\n";

        String basePrompt = "";
        if (scriptFile.getContentType() != null && scriptFile.getContentType().equals("text/plain")) {
            String scriptContent = new String(scriptFile.getBytes(), StandardCharsets.UTF_8);
            basePrompt = "대본 텍스트가 아래와 같이 제공되었습니다:\n" + scriptContent + "\n\n";
        } else if (scriptFile.getContentType() != null && scriptFile.getContentType().contains("pdf")) {
            basePrompt = "대본 PDF 파일이 첨부되었습니다.\n\n";
        } else {
            basePrompt = "대본 파일이 첨부되었습니다.\n\n";
        }

        String prompt = scoreInfo
                + basePrompt
                + "Whisper API로부터 변환된 음성 전사 결과는 아래와 같습니다:\n"
                + whisperTranscript + "\n\n"
                + "위 대본(첨부 파일 또는 텍스트)과 전사 결과를 비교하여, 사용자가 얼마나 정확하게 대본을 읽었는지 평가하고, "
                + "발음, 속도, 긴장도(속도 변화)에 대한 구체적인 피드백과 개선 방안을 제시해주세요.\n\n"
                + "점수의 경우 0점부터 100점까지 부여할 수 있으며, 70점 이상이면 '양호', 90점 이상이면 '우수'로 평가됩니다.\n\n"
                + "대본과 전사 결과를 비교하여, 유사도가 낮아질수록 전체 점수가 낮아질 수 있음을 참고해주세요.\n\n"
                + "점수에 가장 많은 영향을 주는 요소는 대본과 전사결과의 일치도가 1순위, 발음 명확도, 발표 속도, 긴장도(속도 변화)입니다.\n\n"
                + "토픽: " + dto.getIocTopic() + ", 제목: " + dto.getWorkTitle() + ", 작가: " + dto.getAuthor() + "\n\n"
                + "위 정보를 바탕으로, 친절하고 구체적인 피드백을 한국어로 제공해주세요.";
        return prompt;
    }

    /**
     * ChatGPT API의 응답에서 피드백 텍스트를 추출합니다.
     */
    private String extractFeedbackFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error parsing ChatGPT API response", e);
            return "피드백 생성 중 오류가 발생했습니다.";
        }
    }

    /**
     * 분석 결과를 담기 위한 내부 클래스
     */
    public static class AnalysisResult {
        private int pronunciationScore;
        private int speechRateScore;
        private int tensionScore;
        private String unclearSentences;

        public int getPronunciationScore() {
            return pronunciationScore;
        }

        public void setPronunciationScore(int pronunciationScore) {
            this.pronunciationScore = pronunciationScore;
        }

        public int getSpeechRateScore() {
            return speechRateScore;
        }

        public void setSpeechRateScore(int speechRateScore) {
            this.speechRateScore = speechRateScore;
        }

        public int getTensionScore() {
            return tensionScore;
        }

        public void setTensionScore(int tensionScore) {
            this.tensionScore = tensionScore;
        }

        public String getUnclearSentences() {
            return unclearSentences;
        }

        public void setUnclearSentences(String unclearSentences) {
            this.unclearSentences = unclearSentences;
        }
    }
}
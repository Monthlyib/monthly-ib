package com.monthlyib.server.domain.aidescriptive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveTestDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResultDto;
import com.monthlyib.server.api.aidescriptive.dto.SubmitDescriptiveAnswerDto;
import com.monthlyib.server.api.aidescriptive.dto.GptFeedbackResult;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveAnswerRepository;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveTestRepository;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiDescriptiveService {

    private final AiDescriptiveTestRepository descriptiveTestRepository;
    private final AiDescriptiveAnswerRepository answerRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    public AiDescriptiveResponseDto createTest(AiDescriptiveTestDto dto) {
        AiDescriptiveTest test = AiDescriptiveTest.builder()
                .question(dto.getQuestion())
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .maxScore(dto.getMaxScore())
                .build();
        AiDescriptiveTest saved = descriptiveTestRepository.save(test);
        return AiDescriptiveResponseDto.of(saved);
    }

    public Page<AiDescriptiveResponseDto> findBySubjectAndChapter(String subject, String chapter, int page) {
        Page<AiDescriptiveTest> tests = descriptiveTestRepository.findBySubjectAndChapter(subject, chapter,
                PageRequest.of(page, 6, org.springframework.data.domain.Sort.by("createAt").descending()));
        return tests.map(AiDescriptiveResponseDto::of);
    }

    public AiDescriptiveTest findById(Long id) {
        return descriptiveTestRepository.findById(id);
    }

    public AiDescriptiveResponseDto uploadImage(Long id, MultipartFile multipartFile) {
        AiDescriptiveTest test = descriptiveTestRepository.findById(id);

        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AIDESCRIPTIVE_IMAGE);
            test.setImagePath(null);
        }

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String imagePath = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.AIDESCRIPTIVE_IMAGE,
                    "/" + id + "/");
            test.setImagePath(imagePath);
        }

        AiDescriptiveTest saved = descriptiveTestRepository.save(test);
        return AiDescriptiveResponseDto.of(saved);
    }

    public AiDescriptiveResponseDto deleteImage(Long id) {
        AiDescriptiveTest test = descriptiveTestRepository.findById(id);

        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AIDESCRIPTIVE_IMAGE);
            test.setImagePath(null);
        }

        AiDescriptiveTest saved = descriptiveTestRepository.save(test);
        return AiDescriptiveResponseDto.of(saved);
    }

    public AiDescriptiveResponseDto updateTest(Long id, AiDescriptiveTestDto dto) {
        AiDescriptiveTest test = descriptiveTestRepository.findById(id);
        test.setSubject(dto.getSubject());
        test.setChapter(dto.getChapter());
        test.setQuestion(dto.getQuestion());
        test.setMaxScore(dto.getMaxScore());
        AiDescriptiveTest updated = descriptiveTestRepository.save(test);
        return AiDescriptiveResponseDto.of(updated);
    }

    public void deleteTest(Long id) {
        AiDescriptiveTest test = descriptiveTestRepository.findById(id);
        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AIDESCRIPTIVE_IMAGE);
        }
        descriptiveTestRepository.delete(test);
    }

    public AiDescriptiveTest findBySubjectAndChapterOnce(String subject, String chapter) {
        var list = descriptiveTestRepository.findAllBySubjectAndChapter(subject, chapter);
        if (list.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * list.size());
        return list.get(randomIndex);
    }

    public Long submitAnswer(SubmitDescriptiveAnswerDto dto, User user) {
        AiDescriptiveTest test = descriptiveTestRepository.findById(dto.getQuestionId());
        if (test == null || !test.getSubject().equals(dto.getSubject())
                || !test.getChapter().equals(dto.getChapter())) {
            throw new IllegalArgumentException("해당 과목 또는 챕터에 맞는 문제를 찾을 수 없습니다.");
        }

        AiDescriptiveAnswer descriptiveAnswer = AiDescriptiveAnswer.builder()
                .user(user)
                .descriptiveQuestionId(dto.getQuestionId())
                .answerText(dto.getAnswer())
                .maxScore(test.getMaxScore())
                .build();

        answerRepository.save(descriptiveAnswer);
        return descriptiveAnswer.getId();
    }

    public AiDescriptiveResultDto getAnswerResult(Long answerId) {
        AiDescriptiveAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답안을 찾을 수 없습니다."));
        AiDescriptiveTest test = descriptiveTestRepository.findById(answer.getDescriptiveQuestionId());

        AiDescriptiveResultDto resultDto = new AiDescriptiveResultDto();
        resultDto.setQuestionId(test.getId());
        resultDto.setQuestion(test.getQuestion());
        resultDto.setSubject(test.getSubject());
        resultDto.setChapter(test.getChapter());
        resultDto.setMaxScore(test.getMaxScore());
        resultDto.setAnswerId(answer.getId());
        resultDto.setAnswerText(answer.getAnswerText());
        resultDto.setScore(answer.getScore());
        resultDto.setFeedbackEnglish(answer.getFeedbackEnglish());
        resultDto.setFeedbackKorean(answer.getFeedbackKorean());
        resultDto.setModelAnswer(answer.getModelAnswer());
        return resultDto;
    }

    public AiDescriptiveResultDto generateAnswerFeedback(Long answerId, User user) {
        AiDescriptiveAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("답안을 찾을 수 없습니다."));
        AiDescriptiveTest test = descriptiveTestRepository.findById(answer.getDescriptiveQuestionId());
        String subject = test.getSubject();

        // GPT API 호출 (subject에 따라 assistant 선택 필요, 가상의 호출 예시)
        GptFeedbackResult result = callGptFeedbackApi(subject, answer.getAnswerText(), test.getQuestion(),
                test.getMaxScore());

        answer.setScore(result.getScore());
        answer.setMaxScore(result.getMaxScore());
        answer.setModelAnswer(result.getModelAnswer());
        answer.setFeedbackEnglish(result.getImprovementEn());
        answer.setFeedbackKorean(result.getImprovementKo());

        answerRepository.save(answer);

        AiDescriptiveResultDto resultDto = new AiDescriptiveResultDto();
        resultDto.setQuestionId(test.getId());
        resultDto.setQuestion(test.getQuestion());
        resultDto.setSubject(test.getSubject());
        resultDto.setChapter(test.getChapter());
        resultDto.setMaxScore(test.getMaxScore());
        resultDto.setAnswerId(answer.getId());
        resultDto.setAnswerText(answer.getAnswerText());
        resultDto.setScore(answer.getScore());
        resultDto.setFeedbackEnglish(answer.getFeedbackEnglish());
        resultDto.setFeedbackKorean(answer.getFeedbackKorean());
        resultDto.setModelAnswer(answer.getModelAnswer());

        return resultDto;
    }

    private GptFeedbackResult callGptFeedbackApi(String subject, String answerText, String questionText, int maxScore) {
        try {
            Map<String, String> assistantMap = Map.of(
                    "Biology", "asst_Qr7bj6DItbg8hxlgoJm3WrDu",
                    "Chemistry", "asst_35NPwAbblAorxdFbedt5xvJQ",
                    "Physics", "asst_bHLrpIRJEkwEMvgSUjgMgT7i",
                    "English", "asst_english_id",
                    "Econ", "asst_DDWY6eyZ81VgcNi7yYiT3t4u",
                    "Business", "asst_business_id",
                    "Psychology", "asst_XNOHoOkw7onEg1qqqWqS44lY");
            String assistantId = assistantMap.getOrDefault(subject, "asst_default_id").trim();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("OpenAI-Beta", "assistants=v2");

            RestTemplate restTemplate = new RestTemplate();

            try {
                ResponseEntity<JsonNode> assistantsResponse = restTemplate.exchange(
                        "https://api.openai.com/v1/assistants",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        JsonNode.class);
                Logger logger = LoggerFactory.getLogger(AiDescriptiveService.class);
            } catch (Exception ex) {
                Logger logger = LoggerFactory.getLogger(AiDescriptiveService.class);
                logger.warn("Failed to fetch assistant list: {}", ex.getMessage());
            }

            // Create thread
            ResponseEntity<JsonNode> threadResponse = restTemplate.exchange(
                    "https://api.openai.com/v1/threads",
                    HttpMethod.POST,
                    new HttpEntity<>("{}", headers),
                    JsonNode.class);
            String threadId = threadResponse.getBody().path("id").asText();
            Logger logger = LoggerFactory.getLogger(AiDescriptiveService.class);
            // Add message to thread
            ObjectNode messageBody = objectMapper.createObjectNode();
            messageBody.put("role", "user");

            ObjectNode input = objectMapper.createObjectNode();
            input.put("question", questionText);
            input.put("student_answer", answerText);
            input.put("max_score", maxScore);
            messageBody.put("content", input.toString());

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
            JsonNode contentNode = objectMapper.readTree(content);

            return GptFeedbackResult.builder()
                    .score(contentNode.path("score").asInt())
                    .maxScore(contentNode.path("max_score").asInt())
                    .studentAnswer(contentNode.path("student_answer").asText())
                    .modelAnswer(contentNode.path("model_answer").asText())
                    .improvementKo(contentNode.path("improvement").path("ko").asText())
                    .improvementEn(contentNode.path("improvement").path("en").asText())
                    .build();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(AiDescriptiveService.class);
            logger.error("GPT API 호출 중 오류 발생", e);
            throw new RuntimeException("GPT 피드백 생성 실패", e);
        }
    }
}
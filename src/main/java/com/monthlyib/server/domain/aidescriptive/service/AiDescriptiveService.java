package com.monthlyib.server.domain.aidescriptive.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveAnswerResponseDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveAnswerSubmitDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptivePostDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveAnswerJpaRepository;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveTestJpaRepository;
import com.monthlyib.server.domain.aiia.service.OpenAiAssistantService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiDescriptiveService {

    private final AiDescriptiveTestJpaRepository testRepo;
    private final AiDescriptiveAnswerJpaRepository answerRepo;
    private final OpenAiAssistantService openAiService;
    private final FileService fileService;

    @Value("${CHATGPT_FINAL_ASSISTANT_KEY}")
    private String assistantKey;

    private final Gson gson = new Gson();

    public AiDescriptiveResponseDto create(AiDescriptivePostDto dto) {
        AiDescriptiveTest entity = AiDescriptiveTest.create(dto);
        AiDescriptiveTest saved = testRepo.save(entity);
        return AiDescriptiveResponseDto.of(saved);
    }

    @Transactional(readOnly = true)
    public Page<AiDescriptiveResponseDto> findAll(String subject, String chapter, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<AiDescriptiveTest> results = testRepo.findBySubjectAndChapter(subject, chapter, pageable);
        return results.map(AiDescriptiveResponseDto::of);
    }

    @Transactional(readOnly = true)
    public AiDescriptiveResponseDto findById(Long id) {
        AiDescriptiveTest entity = testRepo.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiDescriptiveResponseDto.of(entity);
    }

    @Transactional(readOnly = true)
    public AiDescriptiveResponseDto findBySubjectAndChapter(String subject, String chapter) {
        AiDescriptiveTest entity = testRepo.findFirstBySubjectAndChapterOrderByCreateAtAsc(subject, chapter)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiDescriptiveResponseDto.of(entity);
    }

    public AiDescriptiveResponseDto update(Long id, AiDescriptivePostDto dto) {
        AiDescriptiveTest entity = testRepo.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        entity.update(dto);
        AiDescriptiveTest saved = testRepo.save(entity);
        return AiDescriptiveResponseDto.of(saved);
    }

    public void delete(Long id) {
        testRepo.deleteById(id);
    }

    public AiDescriptiveResponseDto uploadImage(Long id, MultipartFile image) {
        AiDescriptiveTest entity = testRepo.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        String url = fileService.saveMultipartFileForAws(image, AwsProperty.STORAGE, "descriptive/");
        entity.setImagePath(url);
        AiDescriptiveTest saved = testRepo.save(entity);
        return AiDescriptiveResponseDto.of(saved);
    }

    public void deleteImage(Long id) {
        AiDescriptiveTest entity = testRepo.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (entity.getImagePath() != null) {
            fileService.deleteAwsFile(entity.getImagePath());
            entity.setImagePath(null);
            testRepo.save(entity);
        }
    }

    @Transactional(readOnly = true)
    public AiDescriptiveResponseDto getQuestion(String subject, String chapter) {
        AiDescriptiveTest entity = testRepo.findFirstBySubjectAndChapterOrderByCreateAtAsc(subject, chapter)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiDescriptiveResponseDto.of(entity);
    }

    public AiDescriptiveAnswerResponseDto submitAnswer(AiDescriptiveAnswerSubmitDto dto, User user) {
        AiDescriptiveTest test = testRepo.findById(dto.getQuestionId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        AiDescriptiveAnswer answer = AiDescriptiveAnswer.create(
                dto.getQuestionId(),
                dto.getAnswer(),
                test.getMaxScore(),
                user);

        AiDescriptiveAnswer saved = answerRepo.save(answer);
        return AiDescriptiveAnswerResponseDto.of(saved, test);
    }

    @Transactional(readOnly = true)
    public AiDescriptiveAnswerResponseDto getResult(Long answerId) {
        AiDescriptiveAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        AiDescriptiveTest test = testRepo.findById(answer.getDescriptiveQuestionId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiDescriptiveAnswerResponseDto.of(answer, test);
    }

    public AiDescriptiveAnswerResponseDto generateFeedback(Long answerId) {
        AiDescriptiveAnswer answer = answerRepo.findById(answerId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        AiDescriptiveTest test = testRepo.findById(answer.getDescriptiveQuestionId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));

        String prompt = "You are an IB examiner grading a descriptive/written answer. " +
                "Question: " + test.getQuestion() + ". " +
                "Student's answer: " + answer.getAnswerText() + ". " +
                "Max score: " + test.getMaxScore() + ". " +
                "Please evaluate this answer and provide: " +
                "1) A score out of " + test.getMaxScore() + ", " +
                "2) Detailed feedback in English, " +
                "3) Feedback summary in Korean. " +
                "Return ONLY valid JSON: {\"score\": number, \"feedback_english\": \"...\", " +
                "\"feedback_korean\": \"...\", \"model_answer\": \"...\"}";

        String response;
        try {
            response = openAiService.callAssistant(assistantKey, prompt);
        } catch (Exception e) {
            log.warn("Assistant call failed, falling back to chat completion: {}", e.getMessage());
            response = openAiService.chatCompletion("You are an IB examiner.", prompt);
        }

        try {
            // Clean up potential markdown code blocks
            String cleanedResponse = response.trim();
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }
            JsonObject json = gson.fromJson(cleanedResponse, JsonObject.class);

            Integer score = json.has("score") ? json.get("score").getAsInt() : null;
            String feedbackEnglish = json.has("feedback_english") ? json.get("feedback_english").getAsString() : null;
            String feedbackKorean = json.has("feedback_korean") ? json.get("feedback_korean").getAsString() : null;
            String modelAnswer = json.has("model_answer") ? json.get("model_answer").getAsString() : null;

            answer.setFeedback(feedbackEnglish, feedbackKorean, score, modelAnswer);
            AiDescriptiveAnswer saved = answerRepo.save(answer);
            return AiDescriptiveAnswerResponseDto.of(saved, test);
        } catch (Exception e) {
            log.error("Failed to parse feedback JSON response: {}", response, e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

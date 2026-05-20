package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aihistory.entity.AiToolActionType;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.aihistory.model.AiToolHistoryCreateCommand;
import com.monthlyib.server.domain.aihistory.service.AiToolHistoryService;
import com.monthlyib.server.domain.aiia.service.OpenAiAssistantService;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.repository.VoiceFeedbackJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiIoVoiceFeedbackService {

    private final VoiceFeedbackJpaRepository feedbackRepo;
    private final OpenAiAssistantService openAiService;
    private final FileService fileService;
    private final AiToolHistoryService aiToolHistoryService;
    private final ScriptTextExtractionService scriptTextExtractionService;

    public Map<String, Object> createVoiceFeedback(
            MultipartFile audioFile,
            MultipartFile scriptFile,
            String iocTopic,
            String workTitle,
            Integer durationSeconds,
            User user
    ) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.AI_IO_AUDIO_REQUIRED);
        }
        if (scriptFile == null || scriptFile.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_REQUIRED);
        }

        String referenceText = scriptTextExtractionService.extract(scriptFile);
        String transcript = transcribeAudio(audioFile);
        Map<String, Object> deliveryMetrics = buildDeliveryMetrics(transcript, referenceText, durationSeconds);

        String audioUrl = fileService.saveMultipartFileForAws(audioFile, AwsProperty.STORAGE, "aiio-audio/");
        String scriptUrl = fileService.saveMultipartFileForAws(scriptFile, AwsProperty.STORAGE, "aiio-script/");

        VoiceFeedback feedback = VoiceFeedback.create(
                user.getNickName(),
                user.getUserId(),
                iocTopic,
                workTitle
        );
        feedback.setAudioPath(audioUrl);
        feedback.setScriptPath(scriptUrl);
        feedback.setSpeechAnalysis(
                transcript,
                null,
                null,
                null,
                null,
                (Double) deliveryMetrics.get("scriptMatchPercent"),
                null,
                (Double) deliveryMetrics.get("speakingRateWpm"),
                durationSeconds
        );

        VoiceFeedback savedWithTranscript = feedbackRepo.save(feedback);
        String feedbackContent = createOpenAiFeedback(iocTopic, workTitle, referenceText, transcript, deliveryMetrics);
        savedWithTranscript.setFeedbackContent(feedbackContent);
        VoiceFeedback saved = feedbackRepo.save(savedWithTranscript);

        Map<String, Object> result = buildResponse(saved, transcript, deliveryMetrics, audioUrl, scriptUrl);
        recordHistory(user, iocTopic, workTitle, referenceText, saved, result, audioUrl, scriptUrl, durationSeconds, deliveryMetrics);

        return result;
    }

    private String transcribeAudio(MultipartFile audioFile) {
        try {
            String transcript = openAiService.transcribeAudio(audioFile);
            if (!StringUtils.hasText(transcript)) {
                throw new ServiceLogicException(ErrorCode.OPENAI_TRANSCRIPTION_FAILED);
            }
            return transcript.trim();
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.OPENAI_TRANSCRIPTION_FAILED);
        }
    }

    private String createOpenAiFeedback(
            String iocTopic,
            String workTitle,
            String referenceText,
            String transcript,
            Map<String, Object> deliveryMetrics
    ) {
        try {
            String prompt = """
                    You are an IB Language A oral examiner and English delivery coach.
                    Evaluate this student's scripted oral delivery using the reference script and OpenAI transcript.
                    Return Korean markdown only. Use exactly these headings:
                    ## 발화 내용
                    ## 대본 충실도
                    ## 면접 전달력
                    ## 발음/유창성 추정
                    ## 개선 drill

                    Context:
                    Work title: %s
                    IO topic: %s

                    Non-acoustic indicators:
                    Script match percent: %s
                    Speaking rate WPM: %s
                    Duration seconds: %s

                    Recognized transcript:
                    %s

                    Reference script:
                    %s

                    Feedback rules:
                    - Be specific and practical.
                    - Do not claim objective pronunciation, tone, pitch, intonation, or prosody scoring.
                    - You may infer possible delivery issues only from transcript gaps, repeated words, missing words, and speaking rate.
                    - Explain that detailed acoustic tone/intonation scoring is not available in this mode when relevant.
                    - Include 3 concrete drills in the final section.
                    """.formatted(
                    safe(workTitle),
                    safe(iocTopic),
                    value(deliveryMetrics.get("scriptMatchPercent")),
                    value(deliveryMetrics.get("speakingRateWpm")),
                    value(deliveryMetrics.get("durationSeconds")),
                    safe(transcript),
                    truncate(referenceText, 6000)
            );

            return openAiService.chatCompletion("You are an expert IB oral examiner and English delivery coach.", prompt);
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.AI_FEEDBACK_GENERATION_FAILED);
        }
    }

    private Map<String, Object> buildResponse(
            VoiceFeedback feedback,
            String transcript,
            Map<String, Object> deliveryMetrics,
            String audioUrl,
            String scriptUrl
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", feedback.getFeedbackId());
        result.put("feedbackContent", feedback.getFeedbackContent());
        result.put("transcript", transcript);
        result.put("audioUrl", audioUrl);
        result.put("scriptUrl", scriptUrl);
        result.put("deliveryMetrics", deliveryMetrics);
        return result;
    }

    private void recordHistory(
            User user,
            String iocTopic,
            String workTitle,
            String referenceText,
            VoiceFeedback saved,
            Map<String, Object> result,
            String audioUrl,
            String scriptUrl,
            Integer durationSeconds,
            Map<String, Object> deliveryMetrics
    ) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("iocTopic", iocTopic);
        requestPayload.put("workTitle", workTitle);
        requestPayload.put("referenceScript", truncate(referenceText, 6000));

        aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                .user(user)
                .toolType(AiToolType.IO_PRACTICE)
                .actionType(AiToolActionType.VOICE_FEEDBACK)
                .title("AI IO 음성 피드백")
                .summary(truncate(saved.getFeedbackContent(), 180))
                .subject("Language A English")
                .interestTopic(iocTopic)
                .relatedEntityId(saved.getFeedbackId())
                .requestPayload(requestPayload)
                .responsePayload(result)
                .attachmentUrls(List.of(audioUrl, scriptUrl))
                .score(roundToInteger((Double) deliveryMetrics.get("scriptMatchPercent")))
                .maxScore(100)
                .durationSeconds(durationSeconds)
                .build());
    }

    private Map<String, Object> buildDeliveryMetrics(String transcript, String referenceText, Integer durationSeconds) {
        Map<String, Object> map = new HashMap<>();
        map.put("scriptMatchPercent", calculateScriptMatchPercent(transcript, referenceText));
        map.put("speakingRateWpm", calculateSpeakingRate(transcript, durationSeconds));
        map.put("durationSeconds", durationSeconds);
        return map;
    }

    private Double calculateScriptMatchPercent(String transcript, String referenceText) {
        List<String> referenceTokens = tokenize(referenceText);
        if (referenceTokens.isEmpty()) {
            return null;
        }

        Set<String> transcriptTokens = new HashSet<>(tokenize(transcript));
        long matched = referenceTokens.stream()
                .filter(transcriptTokens::contains)
                .count();
        return round((matched * 100D) / referenceTokens.size());
    }

    private Double calculateSpeakingRate(String transcript, Integer durationSeconds) {
        if (!StringUtils.hasText(transcript) || durationSeconds == null || durationSeconds <= 0) {
            return null;
        }
        long wordCount = tokenize(transcript).size();
        return round((wordCount * 60D) / durationSeconds);
    }

    private List<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9' ]", " ")
                        .split("\\s+"))
                .filter(StringUtils::hasText)
                .toList();
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "";
    }

    private String value(Object value) {
        return value == null ? "not available" : String.valueOf(value);
    }

    private Integer roundToInteger(Double value) {
        return value == null ? null : (int) Math.round(value);
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10D) / 10D;
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

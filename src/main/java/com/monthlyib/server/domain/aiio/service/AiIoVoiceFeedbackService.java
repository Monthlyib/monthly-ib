package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aihistory.entity.AiToolActionType;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.aihistory.model.AiToolHistoryCreateCommand;
import com.monthlyib.server.domain.aihistory.service.AiToolHistoryService;
import com.monthlyib.server.domain.aiia.service.OpenAiAssistantService;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.model.AzurePronunciationAssessmentResult;
import com.monthlyib.server.domain.aiio.model.PronunciationIssue;
import com.monthlyib.server.domain.aiio.model.SpeechMetrics;
import com.monthlyib.server.domain.aiio.repository.VoiceFeedbackJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiIoVoiceFeedbackService {

    private final VoiceFeedbackJpaRepository feedbackRepo;
    private final OpenAiAssistantService openAiService;
    private final FileService fileService;
    private final AiToolHistoryService aiToolHistoryService;
    private final ScriptTextExtractionService scriptTextExtractionService;
    private final AzurePronunciationAssessmentService azurePronunciationAssessmentService;

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
        AzurePronunciationAssessmentResult assessment = azurePronunciationAssessmentService.assess(
                audioFile,
                referenceText,
                durationSeconds
        );

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
        SpeechMetrics metrics = assessment.getMetrics();
        feedback.setSpeechAnalysis(
                assessment.getTranscript(),
                assessment.getRawJson(),
                metrics.getPronunciationScore(),
                metrics.getAccuracyScore(),
                metrics.getFluencyScore(),
                metrics.getCompletenessScore(),
                metrics.getProsodyScore(),
                metrics.getSpeakingRateWpm(),
                metrics.getDurationSeconds()
        );

        VoiceFeedback savedWithAnalysis = feedbackRepo.save(feedback);

        String feedbackContent = createOpenAiFeedback(iocTopic, workTitle, referenceText, assessment);
        savedWithAnalysis.setFeedbackContent(feedbackContent);
        VoiceFeedback saved = feedbackRepo.save(savedWithAnalysis);

        Map<String, Object> result = buildResponse(saved, assessment, audioUrl, scriptUrl);
        recordHistory(user, iocTopic, workTitle, referenceText, saved, assessment, result, audioUrl, scriptUrl);

        return result;
    }

    private String createOpenAiFeedback(
            String iocTopic,
            String workTitle,
            String referenceText,
            AzurePronunciationAssessmentResult assessment
    ) {
        try {
            SpeechMetrics metrics = assessment.getMetrics();
            String prompt = """
                    You are an IB Language A oral examiner and English delivery coach.
                    Evaluate this student's scripted oral delivery using the Azure pronunciation assessment scores and transcript.
                    Return Korean markdown only. Use exactly these headings:
                    ## 발음
                    ## 유창성
                    ## 억양/프로소디
                    ## 대본 충실도
                    ## 면접 전달력
                    ## 개선 drill

                    Context:
                    Work title: %s
                    IO topic: %s

                    Scores:
                    Pronunciation: %s
                    Accuracy: %s
                    Fluency: %s
                    Completeness: %s
                    Prosody: %s
                    Speaking rate WPM: %s

                    Recognized transcript:
                    %s

                    Reference script:
                    %s

                    Low-scoring / notable words:
                    %s

                    Feedback rules:
                    - Be specific and practical.
                    - Do not invent issues not supported by the scores/transcript.
                    - Explain how tone, rhythm, pauses, stress, and confidence affect interview/oral delivery.
                    - Include 3 concrete drills in the final section.
                    """.formatted(
                    safe(workTitle),
                    safe(iocTopic),
                    score(metrics.getPronunciationScore()),
                    score(metrics.getAccuracyScore()),
                    score(metrics.getFluencyScore()),
                    score(metrics.getCompletenessScore()),
                    score(metrics.getProsodyScore()),
                    score(metrics.getSpeakingRateWpm()),
                    safe(assessment.getTranscript()),
                    truncate(referenceText, 6000),
                    formatIssues(assessment.getIssues())
            );

            return openAiService.chatCompletion("You are an expert IB oral examiner and English pronunciation coach.", prompt);
        } catch (Exception e) {
            throw new ServiceLogicException(ErrorCode.AI_FEEDBACK_GENERATION_FAILED);
        }
    }

    private Map<String, Object> buildResponse(
            VoiceFeedback feedback,
            AzurePronunciationAssessmentResult assessment,
            String audioUrl,
            String scriptUrl
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("feedbackId", feedback.getFeedbackId());
        result.put("feedbackContent", feedback.getFeedbackContent());
        result.put("transcript", assessment.getTranscript());
        result.put("audioUrl", audioUrl);
        result.put("scriptUrl", scriptUrl);
        result.put("speechMetrics", toMetricMap(assessment.getMetrics()));
        return result;
    }

    private void recordHistory(
            User user,
            String iocTopic,
            String workTitle,
            String referenceText,
            VoiceFeedback saved,
            AzurePronunciationAssessmentResult assessment,
            Map<String, Object> result,
            String audioUrl,
            String scriptUrl
    ) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("iocTopic", iocTopic);
        requestPayload.put("workTitle", workTitle);
        requestPayload.put("referenceScript", truncate(referenceText, 6000));

        Map<String, Object> responsePayload = new HashMap<>(result);
        responsePayload.put("assessmentRawJson", assessment.getRawJson());
        responsePayload.put("pronunciationIssues", assessment.getIssues());

        aiToolHistoryService.recordSuccess(AiToolHistoryCreateCommand.builder()
                .user(user)
                .toolType(AiToolType.IO_PRACTICE)
                .actionType(AiToolActionType.VOICE_FEEDBACK)
                .title("AI IO 음성 분석 피드백")
                .summary(truncate(saved.getFeedbackContent(), 180))
                .subject("Language A English")
                .interestTopic(iocTopic)
                .relatedEntityId(saved.getFeedbackId())
                .requestPayload(requestPayload)
                .responsePayload(responsePayload)
                .attachmentUrls(List.of(audioUrl, scriptUrl))
                .score(roundToInteger(assessment.getMetrics().getPronunciationScore()))
                .maxScore(100)
                .durationSeconds(assessment.getMetrics().getDurationSeconds())
                .build());
    }

    private Map<String, Object> toMetricMap(SpeechMetrics metrics) {
        Map<String, Object> map = new HashMap<>();
        map.put("pronunciationScore", metrics.getPronunciationScore());
        map.put("accuracyScore", metrics.getAccuracyScore());
        map.put("fluencyScore", metrics.getFluencyScore());
        map.put("completenessScore", metrics.getCompletenessScore());
        map.put("prosodyScore", metrics.getProsodyScore());
        map.put("speakingRateWpm", metrics.getSpeakingRateWpm());
        map.put("durationSeconds", metrics.getDurationSeconds());
        return map;
    }

    private String formatIssues(List<PronunciationIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "No notable word-level issues returned.";
        }
        StringBuilder builder = new StringBuilder();
        issues.forEach(issue -> builder
                .append("- ")
                .append(issue.getWord())
                .append(" | accuracy=")
                .append(score(issue.getAccuracyScore()))
                .append(" | error=")
                .append(issue.getErrorType())
                .append('\n'));
        return builder.toString();
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "";
    }

    private String score(Double value) {
        return value == null ? "not available" : String.valueOf(value);
    }

    private Integer roundToInteger(Double value) {
        return value == null ? null : (int) Math.round(value);
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

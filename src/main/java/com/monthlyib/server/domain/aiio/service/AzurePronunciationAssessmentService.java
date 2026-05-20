package com.monthlyib.server.domain.aiio.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentConfig;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGranularity;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGradingSystem;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aiio.model.AzurePronunciationAssessmentResult;
import com.monthlyib.server.domain.aiio.model.PronunciationIssue;
import com.monthlyib.server.domain.aiio.model.SpeechMetrics;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzurePronunciationAssessmentService {

    private static final int DEFAULT_ANALYSIS_TIMEOUT_SECONDS = 180;
    private static final int MAX_ISSUES = 12;

    @Value("${azure.speech.key:}")
    private String speechKey;

    @Value("${azure.speech.region:}")
    private String speechRegion;

    @Value("${azure.speech.language:en-US}")
    private String speechLanguage;

    public AzurePronunciationAssessmentResult assess(MultipartFile audioFile, String referenceText, Integer durationSeconds) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.AI_IO_AUDIO_REQUIRED);
        }
        if (!StringUtils.hasText(speechKey) || !StringUtils.hasText(speechRegion)) {
            throw new ServiceLogicException(ErrorCode.AZURE_SPEECH_NOT_CONFIGURED);
        }

        Path audioPath = writeTempAudio(audioFile);
        SpeechConfig speechConfig = null;
        AudioConfig audioConfig = null;
        SpeechRecognizer recognizer = null;

        try {
            speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage(speechLanguage);
            audioConfig = AudioConfig.fromWavFileInput(audioPath.toString());
            recognizer = new SpeechRecognizer(speechConfig, audioConfig);

            PronunciationAssessmentConfig assessmentConfig = new PronunciationAssessmentConfig(
                    referenceText,
                    PronunciationAssessmentGradingSystem.HundredMark,
                    PronunciationAssessmentGranularity.Phoneme,
                    true
            );
            assessmentConfig.enableProsodyAssessment();
            assessmentConfig.applyTo(recognizer);

            return runContinuousAssessment(recognizer, durationSeconds);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Azure pronunciation assessment failed", e);
            throw new ServiceLogicException(ErrorCode.AZURE_SPEECH_ANALYSIS_FAILED);
        } finally {
            closeQuietly(recognizer);
            closeQuietly(audioConfig);
            closeQuietly(speechConfig);
            try {
                Files.deleteIfExists(audioPath);
            } catch (IOException e) {
                log.debug("Failed to delete temp audio file: {}", audioPath, e);
            }
        }
    }

    private AzurePronunciationAssessmentResult runContinuousAssessment(
            SpeechRecognizer recognizer,
            Integer durationSeconds
    ) throws Exception {
        Semaphore done = new Semaphore(0);
        List<String> transcriptParts = Collections.synchronizedList(new ArrayList<>());
        List<String> rawJsonParts = Collections.synchronizedList(new ArrayList<>());
        AtomicReference<String> cancellationError = new AtomicReference<>();

        recognizer.recognized.addEventListener((sender, event) -> {
            if (event.getResult().getReason() == ResultReason.RecognizedSpeech) {
                if (StringUtils.hasText(event.getResult().getText())) {
                    transcriptParts.add(event.getResult().getText());
                }
                String rawJson = event.getResult().getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult);
                if (StringUtils.hasText(rawJson)) {
                    rawJsonParts.add(rawJson);
                }
            }
        });

        recognizer.canceled.addEventListener((sender, event) -> {
            CancellationDetails details = CancellationDetails.fromResult(event.getResult());
            cancellationError.set(details.toString());
            done.release();
        });

        recognizer.sessionStopped.addEventListener((sender, event) -> done.release());

        recognizer.startContinuousRecognitionAsync().get();
        boolean completed = done.tryAcquire(resolveTimeoutSeconds(durationSeconds), TimeUnit.SECONDS);
        recognizer.stopContinuousRecognitionAsync().get();

        if (!completed) {
            throw new ServiceLogicException(ErrorCode.AZURE_SPEECH_ANALYSIS_FAILED);
        }
        if (StringUtils.hasText(cancellationError.get()) && rawJsonParts.isEmpty()) {
            log.warn("Azure pronunciation assessment canceled: {}", cancellationError.get());
            throw new ServiceLogicException(ErrorCode.AZURE_SPEECH_ANALYSIS_FAILED);
        }
        if (rawJsonParts.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.AZURE_SPEECH_ANALYSIS_FAILED);
        }

        String transcript = String.join(" ", transcriptParts).trim();
        String rawJson = combineRawJson(rawJsonParts);
        SpeechMetrics metrics = aggregateMetrics(rawJsonParts, transcript, durationSeconds);
        List<PronunciationIssue> issues = extractIssues(rawJsonParts);

        return AzurePronunciationAssessmentResult.builder()
                .transcript(transcript)
                .rawJson(rawJson)
                .metrics(metrics)
                .issues(issues)
                .build();
    }

    private Path writeTempAudio(MultipartFile audioFile) {
        try {
            Path tempFile = Files.createTempFile("monthlyib-aiio-", ".wav");
            audioFile.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }
    }

    private int resolveTimeoutSeconds(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return DEFAULT_ANALYSIS_TIMEOUT_SECONDS;
        }
        return Math.max(DEFAULT_ANALYSIS_TIMEOUT_SECONDS, durationSeconds * 2 + 90);
    }

    private String combineRawJson(List<String> rawJsonParts) {
        JsonArray array = new JsonArray();
        rawJsonParts.forEach(rawJson -> array.add(JsonParser.parseString(rawJson)));
        return array.toString();
    }

    private SpeechMetrics aggregateMetrics(List<String> rawJsonParts, String transcript, Integer durationSeconds) {
        MetricAccumulator pron = new MetricAccumulator();
        MetricAccumulator accuracy = new MetricAccumulator();
        MetricAccumulator fluency = new MetricAccumulator();
        MetricAccumulator completeness = new MetricAccumulator();
        MetricAccumulator prosody = new MetricAccumulator();

        rawJsonParts.forEach(rawJson -> {
            JsonObject nbest = firstNBest(rawJson);
            if (nbest == null) {
                return;
            }
            JsonObject assessment = nbest.has("PronunciationAssessment")
                    ? nbest.getAsJsonObject("PronunciationAssessment")
                    : nbest;
            pron.add(getDouble(assessment, "PronScore"));
            accuracy.add(getDouble(assessment, "AccuracyScore"));
            fluency.add(getDouble(assessment, "FluencyScore"));
            completeness.add(getDouble(assessment, "CompletenessScore"));
            prosody.add(getDouble(assessment, "ProsodyScore"));
        });

        return SpeechMetrics.builder()
                .pronunciationScore(pron.average())
                .accuracyScore(accuracy.average())
                .fluencyScore(fluency.average())
                .completenessScore(completeness.average())
                .prosodyScore(prosody.average())
                .speakingRateWpm(calculateSpeakingRate(transcript, durationSeconds))
                .durationSeconds(durationSeconds)
                .build();
    }

    private List<PronunciationIssue> extractIssues(List<String> rawJsonParts) {
        List<PronunciationIssue> issues = new ArrayList<>();

        for (String rawJson : rawJsonParts) {
            JsonObject nbest = firstNBest(rawJson);
            if (nbest == null || !nbest.has("Words")) {
                continue;
            }
            JsonArray words = nbest.getAsJsonArray("Words");
            for (JsonElement element : words) {
                if (issues.size() >= MAX_ISSUES || !element.isJsonObject()) {
                    break;
                }
                JsonObject word = element.getAsJsonObject();
                JsonObject assessment = word.has("PronunciationAssessment")
                        ? word.getAsJsonObject("PronunciationAssessment")
                        : null;
                JsonObject scoreSource = assessment == null ? word : assessment;
                Double accuracyScore = getDouble(scoreSource, "AccuracyScore");
                String errorType = !scoreSource.has("ErrorType")
                        ? "None"
                        : scoreSource.get("ErrorType").getAsString();

                if ((accuracyScore != null && accuracyScore < 70D) || !"None".equalsIgnoreCase(errorType)) {
                    issues.add(PronunciationIssue.builder()
                            .word(getString(word, "Word"))
                            .accuracyScore(accuracyScore)
                            .errorType(errorType)
                            .build());
                }
            }
        }

        return issues;
    }

    private JsonObject firstNBest(String rawJson) {
        JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
        if (!root.has("NBest") || !root.get("NBest").isJsonArray() || root.getAsJsonArray("NBest").isEmpty()) {
            return null;
        }
        return root.getAsJsonArray("NBest").get(0).getAsJsonObject();
    }

    private Double calculateSpeakingRate(String transcript, Integer durationSeconds) {
        if (!StringUtils.hasText(transcript) || durationSeconds == null || durationSeconds <= 0) {
            return null;
        }
        long wordCount = List.of(transcript.trim().split("\\s+")).stream()
                .filter(StringUtils::hasText)
                .count();
        return round((wordCount * 60D) / durationSeconds);
    }

    private Double getDouble(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return round(object.get(key).getAsDouble());
    }

    private String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString();
    }

    private Double round(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10D) / 10D;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            log.debug("Failed to close Azure Speech resource", e);
        }
    }

    private static class MetricAccumulator {
        private double total;
        private int count;

        void add(Double value) {
            if (value == null) {
                return;
            }
            total += value;
            count += 1;
        }

        Double average() {
            if (count == 0) {
                return null;
            }
            return Math.round((total / count) * 10D) / 10D;
        }
    }
}

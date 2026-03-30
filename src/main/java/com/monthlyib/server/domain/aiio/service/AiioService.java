package com.monthlyib.server.domain.aiio.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.aiio.dto.AiioPatchDto;
import com.monthlyib.server.api.aiio.dto.AiioPostDto;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.ai.service.OpenAiClientService;
import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.monthlyib.server.domain.aiio.repository.VoiceFeedbackRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AiioService {

    private final VoiceFeedbackRepository voiceFeedbackRepository;
    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final OpenAiClientService openAiClientService;

    /**
     * AiioPostDto와 사용자 정보를 이용하여
     * 파일 업로드, Whisper API 호출(단어별 세부 정보 포함), 두 단계의 ChatGPT 호출을 통해
     * 최종 VoiceFeedback 엔티티를 생성 및 저장합니다.
     */
    public VoiceFeedback createFeedback(AiioPostDto postDto, User user) {
        try {
            // 0. 사용자 정보 검증
            if (user == null || user.getUserId() == null) {
                log.error("User information is missing: {}", user);
                throw new ServiceLogicException(ErrorCode.NOT_FOUND, "User not authenticated or missing userId");
            }

            // 1. DTO에서 파일 데이터 추출
            MultipartFile scriptFile = postDto.getScriptFile();
            MultipartFile audioFile = postDto.getAudioFile();

            log.warn("scriptFile content type: {}", scriptFile.getContentType());

            // 2. 유니크한 파일 경로 생성
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uniqueScriptPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";
            String uniqueAudioPath = postDto.getIocTopic().replaceAll("\\s+", "_") + "_"
                    + postDto.getWorkTitle().replaceAll("\\s+", "_") + "_" + timestamp + "/";

            // 3. 파일 업로드: S3에 저장 후 반환된 URL 사용
            String scriptFilePath = fileService.saveMultipartFileForAws(scriptFile, AwsProperty.AIIO_SCRIPT,
                    uniqueScriptPath);
            String audioFilePath = fileService.saveMultipartFileForAws(audioFile, AwsProperty.AIIO_AUDIO,
                    uniqueAudioPath);

            // 4. Whisper API 호출: audioFile을 전송하여 verbose JSON 응답 획득
            String whisperVerboseResponse = openAiClientService.transcribe(
                    audioFile,
                    "whisper-1",
                    "verbose_json",
                    null,
                    List.of("word"));
            log.warn("Whisper API Response: {}", whisperVerboseResponse);
            String gpt4oTransResponse = openAiClientService.transcribe(
                    audioFile,
                    "gpt-4o-transcribe",
                    "json",
                    List.of("logprobs"),
                    null);
            log.warn("GPT4oTRANS API Response: {}", gpt4oTransResponse);

            JsonNode whisperRoot = objectMapper.readTree(whisperVerboseResponse);
            log.warn("Whisper Root: {}", whisperRoot);
            String whisperTranscript = whisperRoot.path("text").asText();


            // 6. 첫 번째 ChatGPT 호출: 단어별 분석 지침과 detailedMetrics를 포함한 프롬프트 전송
            String analysisPrompt = "당신은 발표 평가를 위한 단어별 분석 어시스턴트입니다. 데이터를 바탕으로 아래 사항들을 수행하십시오:\n"
                    + "1. 단어별 속도 계산: 각 단어의 길이(문자 수)와 지속시간(단어의 'end' - 'start')을 계산하여 단어별 속도를 산출합니다.\n"
                    + "2. 정확도 평가: 각 단어의 정확도 값을 확인하여 발음이 명확하게 인식되지 않은 단어를 식별합니다.\n"
                    + "3. 속도 변화 분석: 인접 단어들 간의 속도 차이를 분석하여 갑작스런 속도 변화가 나타나는 구간(긴장도가 높은 부분)을 찾아내세요.\n"
                    + "4. 요약 결과 작성: 위 분석 결과를 바탕으로, 문제가 있는 단어나 구간을 목록화하고, 그 단어들이 왜 문제인지 간략히 설명하며, 개선 사항(예: 발음 개선, 속도 조절, 긴장 완화 등)을 제안하는 요약 정보를 생성하세요.\n\n"
                    + "아래는 Whisper API로부터 추출된 단어별 데이로,단어별 속도 정보를 담고 있습니다.:\n\n"
                    + whisperVerboseResponse
                    +"아래는 chat gpt 4o-transcribe API로부터 추출된 단어별 데이터로, 단어별 정확도 정보를 logprob으로 담고 있습니다.:\n\n"
                    + gpt4oTransResponse;
            String analysisSummary = openAiClientService.chat(
                    "당신은 IB Individual Oral 발표의 발음, 속도, 긴장도 패턴을 분석하는 코치입니다. 사용자가 제공한 지시를 따르고 한국어로 명확하게 요약하세요.",
                    analysisPrompt);
            log.warn("Analysis Summary: {}", analysisSummary);

            // 7. 최종 피드백용 프롬프트 구성: 평가 지침, Whisper 전사 결과, 분석 요약, 대본 파일 정보, 토픽/제목/작가 정보 포함
            String finalPrompt = generateFinalPrompt(postDto, whisperTranscript, analysisSummary, scriptFile);

            // 8. 최종 ChatGPT 호출 (assistant를 통한 호출)
            String finalFeedback = openAiClientService.chat(
                    "당신은 IB Individual Oral 발표 평가 코치입니다. 사용자가 제공한 채점 기준을 엄격히 따르고, 결과는 한국어 평문 피드백으로만 작성하세요.",
                    finalPrompt);
            log.warn("Final Feedback: {}", finalFeedback);

            // 9. VoiceFeedback 엔티티 생성 및 저장
            VoiceFeedback voiceFeedback = VoiceFeedback.builder()
                    .iocTopic(postDto.getIocTopic())
                    .workTitle(postDto.getWorkTitle())
                    .author(postDto.getAuthor())
                    .authorId(user.getUserId())
                    .scriptFilePath(scriptFilePath)
                    .audioFilePath(audioFilePath)
                    .feedbackContent(finalFeedback)
                    .build();

            return voiceFeedbackRepository.saveFeedback(voiceFeedback);
        } catch (ServiceLogicException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in createFeedback: ", e);
            throw new ServiceLogicException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 음성 피드백 생성 실패");
        }
    }

    /**
     * 피드백 수정 요청을 처리합니다.
     */
    public VoiceFeedback updateFeedback(AiioPatchDto patchDto, User user) {
        VoiceFeedback voiceFeedback = voiceFeedbackRepository.findFeedbackByAuthorId(user.getUserId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        voiceFeedback.update(patchDto);
        return voiceFeedbackRepository.saveFeedback(voiceFeedback);
    }

    /**
     * 최종 피드백용 프롬프트를 생성합니다.
     * 평가 지침, Whisper 전사 결과, 단어별 분석 요약, 대본 파일 정보, 토픽/제목/작가 정보를 포함합니다.
     */
    private String generateFinalPrompt(AiioPostDto dto, String whisperTranscript, String analysisSummary,
            MultipartFile scriptFile) throws Exception {
        String evaluationGuidelines = "인공지능 발표 평가 및 피드백 어시스턴트 시스템 지침\n\n"
                + "역할과 목표\n"
                + " - 학생의 발표 내용을 정확히 평가하고 친절하며 구체적인 피드백 제공\n"
                + " - 객관적이고 명확한 근거를 제시해 학생의 이해를 돕기\n"
                + " - 전 지구적 이슈(GI)와 문학/비문학 작품의 연결성을 중점 평가\n\n"
                + "채점 절차\n"
                + " - 학생의 답안과 정답 기준 비교 평가\n"
                + " - 정확성, 논리성, 완성도 기반으로 점수 책정 (각 영역 1~10점)\n"
                + " - 감점 요소 점검 및 적용\n\n"
                + "최종 점수(40점 만점)와 등급 산출:\n"
                + " - 35-40점: 7등급\n"
                + " - 30-34점: 6등급\n"
                + " - 25-29점: 5등급\n"
                + " - 20-24점: 4등급\n"
                + " - 15-19점: 3등급\n"
                + " - 0-14점: 1-2등급\n\n"
                + "평가 영역 및 기준\n"
                + " - 영역 A: 작품 이해 및 해석 (10점)\n"
                + "    * 작품 지식, 전 지구적 이슈(GI) 연관성\n"
                + "    * 텍스트 증거 활용\n"
                + "    * 작품 맥락 및 Body of Works(BoW) 활용\n"
                + " - 영역 B: 작품 분석 및 평가 (10점)\n"
                + "    * 작가의 표현 기법 분석(문체, 구조, 수사적 장치)\n"
                + "    * GI와의 명확한 연결성\n"
                + "    * 비판적 평가 및 두 작품 비교의 깊이와 독창성\n"
                + " - 영역 C: 발표 구성 (10점)\n"
                + "    * 발표의 명확한 서론-본문-결론 구조\n"
                + "    * 전환 표현의 활용 및 논리적 흐름\n"
                + "    * GI 초점의 지속성\n"
                + "    * 시간 배분 적절성 (서론: 1분, 본문 각 4분, 결론: 1분)\n"
                + " - 영역 D: 언어 활용 (10점)\n"
                + "    * 어휘·어조·문장 구조의 학술적 정확성\n"
                + "    * 문법 및 전문적 어휘 사용의 일관성\n"
                + "    * 전환 표현(signposts)의 활용\n\n"
                + "감점 요소\n"
                + " - 불필요한 단어 사용\n"
                + "    * 심각(-2점): 30% 이상\n"
                + "    * 준수(-1점): 20% 이상\n"
                + "    * 경미(-0.5점): 15% 이하 빈번히 반복\n"
                + " - 발음 오류\n"
                + "    * 심각(-2점): 주요 용어 5회 이상 오류\n"
                + "    * 준수(-1점): 주요 용어 3~4회 오류\n"
                + "    * 경미(-0.5점): 일반 단어 1~2회 오류\n"
                + " - 시간 관리\n"
                + "    * 심각(-5점): ±2~3분 초과/미달\n"
                + "    * 준수(-2점): ±1~2분 초과/미달\n"
                + "    * 경미(-1점): ±1분 초과/미달\n"
                + " - 발표 전달력\n"
                + "    * 심각(-2점): 이해 어려울 정도의 속도·명확성 문제\n"
                + "    * 준수(-1점): 특정 구간 속도 부적절\n"
                + "    * 경미(-0.5점): 간헐적 전달 실수\n\n"
                + "피드백 작성법\n"
                + " 1. 최종 결과 요약\n"
                + "    - 총점 및 등급 기재\n"
                + "    - 전반적 평가 (1~2문장)\n"
                + " 2. 영역별 피드백\n"
                + "    - 각 영역별 점수\n"
                + "      [강점]: 구체적 예시와 함께 1-2가지 제시\n"
                + "      [개선점]: 명확한 개선 방안과 함께 1-2가지 제시\n"
                + " 3. 감점 요소 안내\n"
                + "    - 감점 항목 (불필요한 단어, 발음 오류, 시간 관리 등)\n"
                + "    - 문제점 구체적 설명\n"
                + "    - 개선 제안 (실천 가능한 방법)\n"
                + " 4. 개선할 점\n"
                + "    - 핵심 개선사항 요약 (1~2문장)\n"
                + "    - 다음 발표에서 구체적으로 어떤 부분을 중점적으로 준비할지 명확히 안내\n"
                + "    - 불필요한 표현을 줄이는 방법 및 구체적인 발표 연습 방안 제공\n\n"
                + "예시\n"
                + " 1. 종합 피드백\n"
                + "    - 총점: 32/40점, 등급: 6등급\n"
                + "    - 전반적 평가: “GI와 작품을 효과적으로 연결했으며, 전반적으로 설득력 있는 발표였습니다. 특히 문학 작품의 분석이 우수했습니다.”\n\n"
                + " 2. 영역별 피드백\n"
                + "    - 영역 A: 작품 이해 및 해석 (8점)\n"
                + "      [강점]: GI와 작품 내 상징적 요소의 연관성을 명확히 제시함\n"
                + "      [개선점]: BoW 활용의 깊이를 더해 발표를 풍부하게 할 것\n"
                + "    - 영역 B: 작품 분석 및 평가 (8점)\n"
                + "      [강점]: 수사적 장치의 분석이 세부적이고 설득력이 있음\n"
                + "      [개선점]: 비문학 작품과의 비교·대조를 좀 더 심화할 것\n"
                + "    - 영역 C: 발표 구성 (7점)\n"
                + "      [강점]: 발표 흐름이 명확하고 서론과 본문 간 전환이 자연스러움\n"
                + "      [개선점]: 결론 부분의 시간 배분을 더 정확하게 조정할 것\n"
                + "    - 영역 D: 언어 활용 (9점)\n"
                + "      [강점]: 어휘가 세련되고 전문적인 표현을 일관되게 사용함\n"
                + "      [개선점]: 불필요한 표현(\"like\", \"umm\")을 더 줄이면 좋겠음\n"
                + " 3. 감점 요소 안내\n"
                + "    - 불필요한 단어 사용 (-0.5점): 발표 중 \"umm\" 표현이 자주 반복됨. 다음 발표에서는 발표 대본을 준비하고 리허설을 통해 줄이세요.\n\n"
                + " 4. 개선할 점\n"
                + "    - 발표의 결론 부분에서 작품 비교를 더 명확히 정리하고, 불필요한 표현을 줄이기 위한 반복적인 리허설을 권장합니다. 다음 발표에서는 이 부분을 개선하면 더욱 높은 점수를 기대할 수 있습니다.\n\n";

        String basePrompt;
        if (scriptFile.getContentType() != null && scriptFile.getContentType().equals("text/plain")) {
            String scriptContent = new String(scriptFile.getBytes(), StandardCharsets.UTF_8);
            basePrompt = "대본 텍스트가 아래와 같이 제공되었습니다:\n" + scriptContent + "\n\n";
        } else if (scriptFile.getContentType() != null && scriptFile.getContentType().contains("pdf")) {
            basePrompt = "대본 PDF 파일이 첨부되었습니다.\n\n";
        } else {
            basePrompt = "대본 파일이 첨부되었습니다.\n\n";
        }

        String finalPrompt = evaluationGuidelines + "\n"
                + "아래는 Whisper API 전사 결과와 단어별 메트릭 데이터 및 어시스턴트가 분석한 결과 요약입니다:\n\n"
                + "Whisper 전사 결과:\n" + whisperTranscript + "\n\n"
                + "단어별 분석 요약:\n" + analysisSummary + "\n\n"
                + "모든 평가는 두가지 원칙을 지키며 이루어져야합니다."
                + "항상 whisper API의 전사 결과를 바탕으로 평가하고, "
                + "어시스턴트가 단어별 분석한 결과를 바탕으로 피드백을 작성해야합니다.\n\n"
                + "만약 whisper API의 전사 결과와 첨부한 대본이 다를 경우,"
                + "어시스턴트는 whisperAPI의 전사결과를 바탕으로 피드백을 작성해야합니다.\n\n"
                + "예를 들어 대본과 whipser API의 전사 결과가 다를 경우,\n"
                + "어시스턴트는 대본을 바탕으로 피드백을 작성하지 않고,\n"
                + "whisper API의 전사 결과를 바탕으로 대본과 다름을 명시하고, 엄청난 감점을 하며 피드백을 작성해야합니다.\n\n"
                + "만약 whisper API의 전사 결과와 첨부한 대본이 비슷할 경우,\n"
                + "어시스턴트는 대본에 대한 평가와 더불어 whisper API의 전사 결과에 대한 피드백을 작성해야합니다.\n\n"
                + basePrompt
                + "위 정보를 바탕으로, 대본(첨부 파일 또는 텍스트)과 사용자가 실제로 말한 내용을 비교하여 다음을 각각 명확하게 구분해서 피드백해 주세요:\n"
                + "발음, 속도, 긴장도(속도 변화)에 대한 구체적인 피드백과 개선 방안을 제시해주세요.\n\n"
                + "다시 한번 강조하지만, 만약 whisper API의 전사 결과와 첨부한 대본이 다를 경우,\n"
                + "어시스턴트는 대본을 바탕으로 피드백을 작성하지 않고,\n"
                + "whisper API의 전사 결과를 바탕으로 대본과 다름을 명시하고, 엄청난 감점을 하며 피드백을 작성해야합니다.\n\n"
                + "  1. 사용자가 개선해야 할 부분 (예: 발음, 속도, 긴장도 등)\n"
                + "  2. 대본 파일에서 수정 또는 보완해야 할 부분\n"
                + "토픽: " + dto.getIocTopic() + ", 제목: " + dto.getWorkTitle() + ", 작가: " + dto.getAuthor();
        return finalPrompt;
    }

}

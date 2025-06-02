package com.monthlyib.server.domain.aidescriptive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveTestDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResultDto;
import com.monthlyib.server.api.aidescriptive.dto.SubmitDescriptiveAnswerDto;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveAnswerRepository;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveTestRepository;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiDescriptiveService {

    private final AiDescriptiveTestRepository descriptiveTestRepository;
    private final AiDescriptiveAnswerRepository answerRepository;
    private final FileService fileService;

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
            String imagePath = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.AIDESCRIPTIVE_IMAGE, "/" + id + "/");
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
        if (test == null || !test.getSubject().equals(dto.getSubject()) || !test.getChapter().equals(dto.getChapter())) {
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
}
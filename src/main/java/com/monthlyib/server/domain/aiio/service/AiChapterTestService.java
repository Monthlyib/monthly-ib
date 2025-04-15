package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.api.aiio.dto.AiChapterTestDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestResponseDto;
import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.repository.AiChapterTestRepository;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AiChapterTestService {

    private final AiChapterTestRepository aiChapterTestRepository;
    private final FileService fileService;

    public AiChapterTestResponseDto createTest(AiChapterTestDto dto) {
        AiChapterTest test = AiChapterTest.builder()
                .question(dto.getQuestion())
                .choiceA(dto.getChoiceA())
                .choiceB(dto.getChoiceB())
                .choiceC(dto.getChoiceC())
                .choiceD(dto.getChoiceD())
                .answer(dto.getAnswer())
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
                .build();

        AiChapterTest saved = aiChapterTestRepository.save(test);

        return AiChapterTestResponseDto.of(saved);
    }

    public AiChapterTestResponseDto uploadImage(Long id, MultipartFile multipartFile) {
        AiChapterTest test = aiChapterTestRepository.findById(id);

        // 기존 이미지가 있다면 삭제
        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AICHAPTER_IMAGE);
        }

        // 새 이미지 저장
        String imagePath = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.AICHAPTER_IMAGE);
        test.setImagePath(imagePath);
        AiChapterTest saved = aiChapterTestRepository.save(test);

        return AiChapterTestResponseDto.of(saved);
    }
    
    public Page<AiChapterTestResponseDto> findBySubjectAndChapter(String subject, String chapter, int page) {
        Page<AiChapterTest> tests = aiChapterTestRepository.findBySubjectAndChapter(subject, chapter, PageRequest.of(page, 6, Sort.by("createAt").descending()));
        return tests.map(AiChapterTestResponseDto::of);
    }

    public AiChapterTestResponseDto findById(Long id) {
        AiChapterTest test = aiChapterTestRepository.findById(id);
        return AiChapterTestResponseDto.of(test);
    }

    public AiChapterTestResponseDto updateTest(Long id, AiChapterTestDto dto) {
        AiChapterTest test = aiChapterTestRepository.findById(id);

        test.setQuestion(dto.getQuestion());
        test.setChoiceA(dto.getChoiceA());
        test.setChoiceB(dto.getChoiceB());
        test.setChoiceC(dto.getChoiceC());
        test.setChoiceD(dto.getChoiceD());
        test.setAnswer(dto.getAnswer());
        test.setSubject(dto.getSubject());
        test.setChapter(dto.getChapter());

        AiChapterTest updated = aiChapterTestRepository.save(test);
        return AiChapterTestResponseDto.of(updated);
    }

    public void deleteTest(Long id) {
        AiChapterTest test = aiChapterTestRepository.findById(id);
        if (test.getImagePath() != null && !test.getImagePath().isEmpty()) {
            fileService.deleteAwsFile(test.getImagePath(), AwsProperty.AICHAPTER_IMAGE);
        }
        aiChapterTestRepository.delete(test);
    }
}

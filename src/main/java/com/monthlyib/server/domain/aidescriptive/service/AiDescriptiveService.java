package com.monthlyib.server.domain.aidescriptive.service;

import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveTestDto;
import com.monthlyib.server.api.aidescriptive.dto.AiDescriptiveResponseDto;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import com.monthlyib.server.domain.aidescriptive.repository.AiDescriptiveTestRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.file.service.FileService;
import com.monthlyib.server.constant.AwsProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class AiDescriptiveService {

    private final AiDescriptiveTestRepository descriptiveTestRepository;
    private final FileService fileService;

    public AiDescriptiveResponseDto createTest(AiDescriptiveTestDto dto) {
        AiDescriptiveTest test = AiDescriptiveTest.builder()
                .question(dto.getQuestion())
                .subject(dto.getSubject())
                .chapter(dto.getChapter())
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
}
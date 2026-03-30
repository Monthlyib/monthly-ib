package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.api.aiio.dto.AiChapterTestPostDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestResponseDto;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.repository.AiChapterTestJpaRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AiChapterTestService {

    private final AiChapterTestJpaRepository testRepository;
    private final FileService fileService;

    public AiChapterTestResponseDto create(AiChapterTestPostDto dto) {
        AiChapterTest entity = AiChapterTest.create(dto);
        AiChapterTest saved = testRepository.save(entity);
        return AiChapterTestResponseDto.of(saved);
    }

    @Transactional(readOnly = true)
    public Page<AiChapterTestResponseDto> findAll(String subject, String chapter, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<AiChapterTest> results = testRepository.findBySubjectAndChapter(subject, chapter, pageable);
        return results.map(AiChapterTestResponseDto::of);
    }

    @Transactional(readOnly = true)
    public AiChapterTestResponseDto findById(Long id) {
        AiChapterTest entity = testRepository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiChapterTestResponseDto.of(entity);
    }

    public AiChapterTestResponseDto update(Long id, AiChapterTestPostDto dto) {
        AiChapterTest entity = testRepository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        entity.setSubject(dto.getSubject());
        entity.setChapter(dto.getChapter());
        entity.setQuestion(dto.getQuestion());
        entity.setAnswer(dto.getAnswer());
        entity.setChoiceA(dto.getChoiceA());
        entity.setChoiceB(dto.getChoiceB());
        entity.setChoiceC(dto.getChoiceC());
        entity.setChoiceD(dto.getChoiceD());
        AiChapterTest saved = testRepository.save(entity);
        return AiChapterTestResponseDto.of(saved);
    }

    public void delete(Long id) {
        testRepository.deleteById(id);
    }

    public AiChapterTestResponseDto uploadImage(Long id, MultipartFile image) {
        AiChapterTest entity = testRepository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        String url = fileService.saveMultipartFileForAws(image, AwsProperty.STORAGE, "aiio/");
        entity.setImagePath(url);
        AiChapterTest saved = testRepository.save(entity);
        return AiChapterTestResponseDto.of(saved);
    }

    public void deleteImage(Long id) {
        AiChapterTest entity = testRepository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (entity.getImagePath() != null) {
            fileService.deleteAwsFile(entity.getImagePath());
            entity.setImagePath(null);
            testRepository.save(entity);
        }
    }
}

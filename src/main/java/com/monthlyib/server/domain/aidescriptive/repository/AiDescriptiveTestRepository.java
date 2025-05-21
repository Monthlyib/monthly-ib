package com.monthlyib.server.domain.aidescriptive.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;

public interface AiDescriptiveTestRepository {
    AiDescriptiveTest save(AiDescriptiveTest test);
    AiDescriptiveTest findById(Long id);
    Page<AiDescriptiveTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable);
    List<AiDescriptiveTest> findAllBySubjectAndChapter(String subject, String chapter);
    void delete(AiDescriptiveTest test);
}

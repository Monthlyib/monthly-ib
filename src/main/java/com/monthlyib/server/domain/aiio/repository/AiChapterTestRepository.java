package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiChapterTestRepository {
    AiChapterTest save(AiChapterTest test);

    AiChapterTest findById(Long id);

    Page<AiChapterTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable);
    
    void delete(AiChapterTest test); 
}
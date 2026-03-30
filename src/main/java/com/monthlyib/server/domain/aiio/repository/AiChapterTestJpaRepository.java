package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChapterTestJpaRepository extends JpaRepository<AiChapterTest, Long> {

    Page<AiChapterTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable);
}

package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiDescriptiveTestJpaRepository extends JpaRepository<AiDescriptiveTest, Long> {

    Page<AiDescriptiveTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable);

    Optional<AiDescriptiveTest> findFirstBySubjectAndChapterOrderByCreateAtAsc(String subject, String chapter);
}

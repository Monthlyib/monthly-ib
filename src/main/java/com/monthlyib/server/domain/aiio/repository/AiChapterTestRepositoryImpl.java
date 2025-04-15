package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aiio.entity.AiChapterTest;
import com.monthlyib.server.domain.aiio.entity.QAiChapterTest;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AiChapterTestRepositoryImpl extends QuerydslRepositorySupport implements AiChapterTestRepository {

    private final AiChapterTestJpaRepository jpaRepository;

    public AiChapterTestRepositoryImpl(AiChapterTestJpaRepository jpaRepository) {
        super(AiChapterTest.class);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AiChapterTest save(AiChapterTest test) {
        return jpaRepository.save(test);
    }

    @Override
    public AiChapterTest findById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    @Override
    public Page<AiChapterTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable) {
        QAiChapterTest qTest = QAiChapterTest.aiChapterTest;

        JPQLQuery<AiChapterTest> query = from(qTest)
                .where(qTest.subject.eq(subject).and(qTest.chapter.eq(chapter)));

        List<AiChapterTest> content = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();

        return new PageImpl<>(content, pageable, query.fetchCount());
    }

    @Override
    public void delete(AiChapterTest test) {
        jpaRepository.delete(test);
    }

    @Override
    public List<AiChapterTest> findAllBySubjectAndChapter(String subject, String chapter) {
        QAiChapterTest qTest = QAiChapterTest.aiChapterTest;

        return from(qTest)
                .where(qTest.subject.eq(subject).and(qTest.chapter.eq(chapter)))
                .fetch();
    }
}
package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveTest;
import com.monthlyib.server.domain.aidescriptive.entity.QAiDescriptiveTest;
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
public class AiDescriptiveTestRepositoryImpl extends QuerydslRepositorySupport implements AiDescriptiveTestRepository {

    private final AiDescriptiveTestJpaRepository jpaRepository;

    public AiDescriptiveTestRepositoryImpl(AiDescriptiveTestJpaRepository jpaRepository) {
        super(AiDescriptiveTest.class);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AiDescriptiveTest save(AiDescriptiveTest test) {
        return jpaRepository.save(test);
    }

    @Override
    public AiDescriptiveTest findById(Long id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }

    @Override
    public Page<AiDescriptiveTest> findBySubjectAndChapter(String subject, String chapter, Pageable pageable) {
        QAiDescriptiveTest qTest = QAiDescriptiveTest.aiDescriptiveTest;

        JPQLQuery<AiDescriptiveTest> query = from(qTest)
            .where(qTest.subject.eq(subject).and(qTest.chapter.eq(chapter)));

        List<AiDescriptiveTest> content = Optional.ofNullable(getQuerydsl())
            .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
            .applyPagination(pageable, query)
            .fetch();

        return new PageImpl<>(content, pageable, query.fetchCount());
    }

    @Override
    public void delete(AiDescriptiveTest test) {
        jpaRepository.delete(test);
    }

    @Override
    public List<AiDescriptiveTest> findAllBySubjectAndChapter(String subject, String chapter) {
        QAiDescriptiveTest qTest = QAiDescriptiveTest.aiDescriptiveTest;

        return from(qTest)
            .where(qTest.subject.eq(subject).and(qTest.chapter.eq(chapter)))
            .fetch();
    }
}

package com.monthlyib.server.domain.aiio.repository;

import com.monthlyib.server.domain.aiio.entity.VoiceFeedback;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class VoiceFeedbackRepositoryImpl extends QuerydslRepositorySupport implements VoiceFeedbackRepository {

    private final VoiceFeedbackJpaRepository voiceFeedbackJpaRepository;
    private final JPAQueryFactory queryFactory;

    // 수정 전: 외부에서 JPAQueryFactory를 주입받음
    // public VoiceFeedbackRepositoryImpl(VoiceFeedbackJpaRepository voiceFeedbackJpaRepository, JPAQueryFactory queryFactory) {
    //     super(VoiceFeedback.class);
    //     this.voiceFeedbackJpaRepository = voiceFeedbackJpaRepository;
    //     this.queryFactory = queryFactory;
    // }

    // 수정 후: EntityManager를 이용해 내부에서 JPAQueryFactory를 생성
    public VoiceFeedbackRepositoryImpl(VoiceFeedbackJpaRepository voiceFeedbackJpaRepository) {
        super(VoiceFeedback.class);
        this.voiceFeedbackJpaRepository = voiceFeedbackJpaRepository;
        this.queryFactory = new JPAQueryFactory(getEntityManager());
    }

    @Override
    public Optional<VoiceFeedback> findFeedback(Long feedbackId) {
        return voiceFeedbackJpaRepository.findById(feedbackId);
    }

    @Override
    public Page<VoiceFeedback> findAllFeedback(Pageable pageable) {
        return voiceFeedbackJpaRepository.findAll(pageable);
    }

    @Override
    public VoiceFeedback saveFeedback(VoiceFeedback voiceFeedback) {
        return voiceFeedbackJpaRepository.save(voiceFeedback);
    }

    @Override
    public void deleteFeedback(Long feedbackId) {
        voiceFeedbackJpaRepository.deleteById(feedbackId);
    }
    
    @Override
    public Optional<VoiceFeedback> findFeedbackByAuthorId(Long authorId) {
        return voiceFeedbackJpaRepository.findByAuthorId(authorId);
    }
}
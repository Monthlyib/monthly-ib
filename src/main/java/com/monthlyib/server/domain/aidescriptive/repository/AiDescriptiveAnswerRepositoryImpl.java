package com.monthlyib.server.domain.aidescriptive.repository;

import com.monthlyib.server.domain.aidescriptive.entity.AiDescriptiveAnswer;
import com.monthlyib.server.domain.aidescriptive.entity.QAiDescriptiveAnswer;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.constant.ErrorCode;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AiDescriptiveAnswerRepositoryImpl extends QuerydslRepositorySupport implements AiDescriptiveAnswerRepository {

    private final AiDescriptiveAnswerJpaRepository jpaRepository;

    public AiDescriptiveAnswerRepositoryImpl(AiDescriptiveAnswerJpaRepository jpaRepository) {
        super(AiDescriptiveAnswer.class);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AiDescriptiveAnswer save(AiDescriptiveAnswer answer) {
        return jpaRepository.save(answer);
    }

    @Override
    public Optional<AiDescriptiveAnswer> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<AiDescriptiveAnswer> findByUserAndDescriptiveQuestionId(User user, Long descriptiveQuestionId) {
        QAiDescriptiveAnswer qAnswer = QAiDescriptiveAnswer.aiDescriptiveAnswer;

        JPQLQuery<AiDescriptiveAnswer> query = from(qAnswer)
            .where(qAnswer.user.eq(user)
                .and(qAnswer.descriptiveQuestionId.eq(descriptiveQuestionId)));

        return Optional.ofNullable(query.fetchOne());
    }
}

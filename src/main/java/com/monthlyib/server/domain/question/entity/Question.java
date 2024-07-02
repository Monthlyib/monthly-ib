package com.monthlyib.server.domain.question.entity;


import com.monthlyib.server.api.question.dto.QuestionPatchDto;
import com.monthlyib.server.api.question.dto.QuestionPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "questions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String authorNickName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionStatus questionStatus;

    @Column(nullable = false)
    private Long answerId;

    public static Question create(QuestionPostDto dto, User user) {
        return Question.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .subject(dto.getSubject())
                .authorId(dto.getAuthorId())
                .authorUsername(user.getUsername())
                .authorNickName(user.getNickName())
                .questionStatus(QuestionStatus.ANSWER_WAIT)
                .answerId(0L)
                .build();
    }

    public Question update(QuestionPatchDto dto) {
        this.title = Optional.ofNullable(dto.getTitle()).orElse(this.title);
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        this.subject = Optional.ofNullable(dto.getSubject()).orElse(this.subject);
        this.questionStatus = Optional.ofNullable(dto.getQuestionStatus()).orElse(this.questionStatus);
        return this;
    }

}

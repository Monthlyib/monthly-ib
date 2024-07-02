package com.monthlyib.server.domain.answer.entity;


import com.monthlyib.server.api.question.dto.AnswerPatchDto;
import com.monthlyib.server.api.question.dto.AnswerPostDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "answers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

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

    @Column(nullable = false, unique = true)
    private Long questionId;

    public static Answer create(AnswerPostDto dto, QuestionResponseDto questionResponseDto, UserResponseDto user) {
        return Answer.builder()
                .content(dto.getContent())
                .subject(questionResponseDto.getSubject())
                .authorId(user.getUserId())
                .authorUsername(user.getUsername())
                .authorNickName(user.getNickName())
                .questionId(questionResponseDto.getQuestionId())
                .build();
    }

    public Answer update(AnswerPatchDto dto) {
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.content);
        return this;
    }

}

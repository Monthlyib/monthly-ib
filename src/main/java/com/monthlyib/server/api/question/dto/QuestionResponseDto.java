package com.monthlyib.server.api.question.dto;

import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.answer.entity.Answer;
import com.monthlyib.server.domain.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {

    private Long questionId;

    private String title;

    private String content;

    private String subject;

    private Long authorId;

    private String authorUsername;

    private String authorNickName;

    private QuestionStatus questionStatus;

    private AnswerResponseDto answer;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public QuestionResponseDto(
            Long questionId,
            String title,
            String content,
            String subject,
            Long authorId,
            String authorUsername,
            String authorNickName,
            QuestionStatus questionStatus,
            LocalDateTime createAt,
            LocalDateTime updateAt
    ) {
        this.questionId = questionId;
        this.title = title;
        this.content = content;
        this.subject = subject;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorNickName = authorNickName;
        this.questionStatus = questionStatus;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }



    public static QuestionResponseDto of(Question question, Answer answer) {
        return QuestionResponseDto.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .subject(question.getSubject())
                .authorId(question.getAuthorId())
                .authorUsername(question.getAuthorUsername())
                .authorNickName(question.getAuthorNickName())
                .questionStatus(question.getQuestionStatus())
                .answer(AnswerResponseDto.of(answer))
                .createAt(question.getCreateAt())
                .updateAt(question.getUpdateAt())
                .build();
    }

}

package com.monthlyib.server.api.question.dto;

import com.monthlyib.server.constant.QuestionStatus;
import com.monthlyib.server.domain.answer.entity.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerResponseDto {

    private Long questionId;

    private Long answerId;

    private String content;

    private Long authorId;

    private String authorUsername;

    private String authorNickName;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static AnswerResponseDto of(Answer answer) {
        if (answer == null) {
            return null;
        }
        return AnswerResponseDto.builder()
                .questionId(answer.getQuestionId())
                .answerId(answer.getAnswerId())
                .content(answer.getContent())
                .authorId(answer.getAuthorId())
                .authorUsername(answer.getAuthorUsername())
                .authorNickName(answer.getAuthorNickName())
                .createAt(answer.getCreateAt())
                .updateAt(answer.getUpdateAt())
                .build();
    }

}

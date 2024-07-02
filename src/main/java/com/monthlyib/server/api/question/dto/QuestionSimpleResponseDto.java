package com.monthlyib.server.api.question.dto;

import com.monthlyib.server.constant.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionSimpleResponseDto {

    private Long questionId;

    private String title;

    private String subject;

    private Long authorId;

    private String authorUsername;

    private String authorNickName;

    private QuestionStatus questionStatus;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

}

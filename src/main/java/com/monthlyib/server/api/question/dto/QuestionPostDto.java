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
public class QuestionPostDto {

    private String title;

    private String content;

    private String subject;

    private Long authorId;

}

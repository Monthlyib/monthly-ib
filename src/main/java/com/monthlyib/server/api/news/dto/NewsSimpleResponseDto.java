package com.monthlyib.server.api.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsSimpleResponseDto {

    private Long newsId;

    private Long authorUserId;

    private String authorUsername;

    private String authorNickName;

    private String title;

    private String content;

    private long viewCount;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

}

package com.monthlyib.server.api.news.dto;

import com.monthlyib.server.domain.news.entity.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsResponseDto {

    private Long newsId;

    private Long authorUserId;

    private String authorUsername;

    private String authorNickName;

    private String title;

    private String content;

    private long viewCount;

    private List<NewsFileResponseDto> files;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;


    public NewsResponseDto(
            Long newsId,
            Long authorUserId,
            String authorUsername,
            String authorNickName,
            String title,
            String content,
            long viewCount,
            LocalDateTime createAt,
            LocalDateTime updateAt
    ) {
        this.newsId = newsId;
        this.authorUserId = authorUserId;
        this.authorUsername = authorUsername;
        this.authorNickName = authorNickName;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public static NewsResponseDto of(News news, List<NewsFileResponseDto> files) {
        return NewsResponseDto.builder()
                .newsId(news.getNewsId())
                .authorUserId(news.getAuthorId())
                .authorUsername(news.getAuthorUsername())
                .authorNickName(news.getAuthorNickName())
                .title(news.getTitle())
                .content(news.getContent())
                .viewCount(news.getViewCount())
                .files(files)
                .createAt(news.getCreateAt())
                .updateAt(news.getUpdateAt())
                .build();
    }

    public static NewsResponseDto of(News news) {
        return NewsResponseDto.builder()
                .newsId(news.getNewsId())
                .authorUserId(news.getAuthorId())
                .authorUsername(news.getAuthorUsername())
                .authorNickName(news.getAuthorNickName())
                .title(news.getTitle())
                .content(news.getContent())
                .viewCount(news.getViewCount())
                .files(List.of())
                .createAt(news.getCreateAt())
                .updateAt(news.getUpdateAt())
                .build();
    }


}

package com.monthlyib.server.api.news.dto;

import com.monthlyib.server.domain.news.entity.NewsFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsFileResponseDto {

    private Long fileId;

    private Long newsId;

    private String fileName;

    private String fileUrl;

    public static NewsFileResponseDto of(NewsFile newsFile) {
        return NewsFileResponseDto.builder()
                .fileId(newsFile.getNewsFileId())
                .newsId(newsFile.getNewsId())
                .fileName(newsFile.getFileName())
                .fileUrl(newsFile.getUrl())
                .build();
    }

}

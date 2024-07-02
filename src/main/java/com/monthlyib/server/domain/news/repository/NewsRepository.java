package com.monthlyib.server.domain.news.repository;

import com.monthlyib.server.api.news.dto.NewsResponseDto;
import com.monthlyib.server.api.news.dto.NewsSearchDto;
import com.monthlyib.server.api.news.dto.NewsSimpleResponseDto;
import com.monthlyib.server.domain.news.entity.News;
import com.monthlyib.server.domain.news.entity.NewsFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NewsRepository {

    Page<NewsSimpleResponseDto> findAllNews(Pageable pageable, NewsSearchDto dto);

    NewsResponseDto findNews(Long newsId);

    Optional<News> findNewsEntity(Long newsId);

    Optional<NewsFile> findNewsFile(Long newsFileId);

    News saveNews(News news);

    NewsFile saveNewsFile(NewsFile newsFile);

    List<NewsFile> findAllNewsFile(Long newsId);

    void deleteNews(Long newsId);

    void deleteNewsFile(Long newsFileId);

    void deleteAllNewsFile(Long newsId);
}

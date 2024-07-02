package com.monthlyib.server.domain.news.repository;


import com.monthlyib.server.api.board.dto.BoardSimpleResponseDto;
import com.monthlyib.server.api.news.dto.NewsFileResponseDto;
import com.monthlyib.server.api.news.dto.NewsResponseDto;
import com.monthlyib.server.api.news.dto.NewsSearchDto;
import com.monthlyib.server.api.news.dto.NewsSimpleResponseDto;
import com.monthlyib.server.api.question.dto.QuestionResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.news.entity.News;
import com.monthlyib.server.domain.news.entity.NewsFile;
import com.monthlyib.server.domain.news.entity.QNews;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NewsRepositoryImpl extends QuerydslRepositorySupport implements NewsRepository {

    private final NewsJpaRepository newsJpaRepository;

    private final NewsFileJpaRepository newsFileJpaRepository;

    public NewsRepositoryImpl(NewsJpaRepository newsJpaRepository, NewsFileJpaRepository newsFileJpaRepository) {
        super(News.class);
        this.newsJpaRepository = newsJpaRepository;
        this.newsFileJpaRepository = newsFileJpaRepository;
    }

    QNews news = QNews.news;

    @Override
    public Page<NewsSimpleResponseDto> findAllNews(Pageable pageable, NewsSearchDto dto) {

        String keyWord = dto.getKeyWord();
        JPQLQuery<NewsSimpleResponseDto> query = getNewsSimpleQuery();
        if (keyWord != null) {
            query.where(news.title.containsIgnoreCase(keyWord)
                    .or(news.content.containsIgnoreCase(keyWord)));
        }
        List<NewsSimpleResponseDto> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public NewsResponseDto findNews(Long newsId) {
        News findNews = newsJpaRepository.findById(newsId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        List<NewsFileResponseDto> findNewsFile = newsFileJpaRepository.findAllByNewsId(newsId)
                .stream().map(NewsFileResponseDto::of).toList();
        return NewsResponseDto.of(findNews, findNewsFile);
    }

    @Override
    public Optional<News> findNewsEntity(Long newsId) {
        return newsJpaRepository.findById(newsId);
    }

    @Override
    public Optional<NewsFile> findNewsFile(Long newsFileId) {
        return newsFileJpaRepository.findById(newsFileId);
    }

    @Override
    public News saveNews(News news) {
        return newsJpaRepository.save(news);
    }

    @Override
    public NewsFile saveNewsFile(NewsFile newsFile) {
        return newsFileJpaRepository.save(newsFile);
    }

    @Override
    public List<NewsFile> findAllNewsFile(Long newsId) {
        return newsFileJpaRepository.findAllByNewsId(newsId);
    }

    @Override
    public void deleteNews(Long newsId) {
        newsFileJpaRepository.deleteAllByNewsId(newsId);
        newsJpaRepository.deleteById(newsId);
    }

    @Override
    public void deleteNewsFile(Long newsFileId) {
        newsFileJpaRepository.deleteById(newsFileId);
    }

    @Override
    public void deleteAllNewsFile(Long newsId) {
        newsFileJpaRepository.deleteAllByNewsId(newsId);
    }

    private JPQLQuery<NewsSimpleResponseDto> getNewsSimpleQuery() {

        return from(news)
                .select(
                        Projections.constructor(
                                NewsSimpleResponseDto.class,
                                news.newsId,
                                news.authorId,
                                news.authorUsername,
                                news.authorNickName,
                                news.title,
                                news.content,
                                news.viewCount,
                                news.createAt,
                                news.updateAt
                        )
                );
    }

    private JPQLQuery<NewsResponseDto> getNewsQuery() {
        return from(news)
                .select(
                        Projections.constructor(
                                NewsResponseDto.class,
                                news.newsId,
                                news.authorId,
                                news.authorUsername,
                                news.authorNickName,
                                news.title,
                                news.content,
                                news.viewCount,
                                news.createAt,
                                news.updateAt
                        )
                );
    }
}

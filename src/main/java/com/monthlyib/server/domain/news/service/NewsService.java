package com.monthlyib.server.domain.news.service;

import com.monthlyib.server.api.news.dto.*;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.news.entity.News;
import com.monthlyib.server.domain.news.entity.NewsFile;
import com.monthlyib.server.domain.news.repository.NewsRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;

    private final FileService fileService;

    public Page<NewsSimpleResponseDto> findAllNews(int page, NewsSearchDto dto) {
        return newsRepository.findAllNews(PageRequest.of(page, 5, Sort.by("createAt").descending()), dto);
    }

    public NewsResponseDto findNews(Long newsId) {
        return newsRepository.findNews(newsId);
    }

    public NewsResponseDto createNews(NewsPostDto dto, User user) {
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        News createNews = News.create(dto, user);
        News saveNews = newsRepository.saveNews(createNews);
        return NewsResponseDto.of(saveNews);
    }

    public NewsResponseDto updateNews(Long newsId, NewsPatchDto dto, User user) {
        if (!user.getAuthority().equals(Authority.ADMIN)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        News findNews = verifyNews(newsId);
        findNews.setTitle(Optional.ofNullable(dto.getTitle()).orElse(findNews.getTitle()));
        findNews.setContent(Optional.ofNullable(dto.getContent()).orElse(findNews.getContent()));
        News saveNews = newsRepository.saveNews(findNews);
        List<NewsFileResponseDto> file = newsRepository.findAllNewsFile(newsId).stream().map(NewsFileResponseDto::of).toList();
        return NewsResponseDto.of(saveNews, file);
    }

    public NewsResponseDto createOrUpdatreNewsFile(Long newsId, MultipartFile[] file, User user) {
        News findNews = verifyNews(newsId);
        newsRepository.deleteAllNewsFile(newsId);
        List<NewsFileResponseDto> list = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            String url = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.NEWS_FILE);
            String filename = multipartFile.getOriginalFilename();
            NewsFile newFile = NewsFile.create(newsId, filename, url);
            NewsFile saveFile = newsRepository.saveNewsFile(newFile);
            list.add(NewsFileResponseDto.of(saveFile));
        }
        return NewsResponseDto.of(findNews, list);
    }

    public void deleteNews(Long newsId, User user) {
        newsRepository.deleteNews(newsId);
    }

    private News verifyNews(Long newsId) {
        return newsRepository.findNewsEntity(newsId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    }
}

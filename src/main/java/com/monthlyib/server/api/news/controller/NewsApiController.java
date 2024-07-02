package com.monthlyib.server.api.news.controller;

import com.monthlyib.server.api.news.dto.*;
import com.monthlyib.server.domain.news.service.NewsService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.utils.StubUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class NewsApiController implements NewsApiControllerIfs{

    private final NewsService newsService;

    @Override
    @GetMapping("/open-api/news")
    public ResponseEntity<PageResponseDto<?>> getNewsList(int page, NewsSearchDto requestDto) {
        Page<NewsSimpleResponseDto> response = newsService.findAllNews(page, requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/api/news/{newsId}")
    public ResponseEntity<ResponseDto<?>> getNews(Long newsId) {
        NewsResponseDto response = newsService.findNews(newsId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/news")
    public ResponseEntity<ResponseDto<?>> postNews(NewsPostDto requestDto, User user) {
        NewsResponseDto response = newsService.createNews(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/news/{newsId}")
    public ResponseEntity<ResponseDto<?>> patchNews(Long newsId, NewsPatchDto requestDto, User user) {
        NewsResponseDto response = newsService.updateNews(newsId, requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/news/news-file/{newsId}")
    public ResponseEntity<ResponseDto<?>> postNewsFile(Long newsId, MultipartFile[] multipartFile, User user) {
        NewsResponseDto response = newsService.createOrUpdatreNewsFile(newsId, multipartFile, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/news/{newsId}")
    public ResponseEntity<ResponseDto<?>> deleteNews(Long newsId, User user) {
        newsService.deleteNews(newsId, user);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}

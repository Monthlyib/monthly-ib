package com.monthlyib.server.api.aiio.controller;

import com.monthlyib.server.api.aiio.dto.AiChapterTestPostDto;
import com.monthlyib.server.api.aiio.dto.AiChapterTestResponseDto;
import com.monthlyib.server.domain.aiio.service.AiChapterTestService;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chapter-test")
@RequiredArgsConstructor
@Slf4j
public class AiChapterTestController {

    private final AiChapterTestService aiChapterTestService;

    @PostMapping
    public ResponseDto<AiChapterTestResponseDto> create(@RequestBody AiChapterTestPostDto dto) {
        AiChapterTestResponseDto result = aiChapterTestService.create(dto);
        return ResponseDto.of(result, Result.ok());
    }

    @PatchMapping("/{id}")
    public ResponseDto<AiChapterTestResponseDto> update(
            @PathVariable Long id,
            @RequestBody AiChapterTestPostDto dto) {
        AiChapterTestResponseDto result = aiChapterTestService.update(id, dto);
        return ResponseDto.of(result, Result.ok());
    }

    @PostMapping("/image/{id}")
    public ResponseDto<AiChapterTestResponseDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        AiChapterTestResponseDto result = aiChapterTestService.uploadImage(id, image);
        return ResponseDto.of(result, Result.ok());
    }

    @DeleteMapping("/image/{id}")
    public ResponseDto<Result> deleteImage(@PathVariable Long id) {
        aiChapterTestService.deleteImage(id);
        return ResponseDto.of(Result.ok());
    }

    @GetMapping("/list")
    public PageResponseDto<Page<AiChapterTestResponseDto>> findAll(
            @RequestParam String subject,
            @RequestParam String chapter,
            @RequestParam(defaultValue = "0") int page) {
        Page<AiChapterTestResponseDto> result = aiChapterTestService.findAll(subject, chapter, page);
        return PageResponseDto.of(result, result, Result.ok());
    }

    @DeleteMapping("/{id}")
    public ResponseDto<Result> delete(@PathVariable Long id) {
        aiChapterTestService.delete(id);
        return ResponseDto.of(Result.ok());
    }

    @GetMapping("/{id}")
    public ResponseDto<AiChapterTestResponseDto> findById(@PathVariable Long id) {
        AiChapterTestResponseDto result = aiChapterTestService.findById(id);
        return ResponseDto.of(result, Result.ok());
    }
}

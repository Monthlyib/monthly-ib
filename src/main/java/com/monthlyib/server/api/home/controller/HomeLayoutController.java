package com.monthlyib.server.api.home.controller;

import com.monthlyib.server.api.home.dto.HomeLayoutContentDto;
import com.monthlyib.server.domain.home.service.HomeLayoutService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class HomeLayoutController {

    private final HomeLayoutService homeLayoutService;

    @GetMapping("/open-api/home-layout")
    public ResponseEntity<ResponseDto<?>> getPublishedLayout() {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.getPublishedLayout(), Result.ok()));
    }

    @GetMapping("/api/admin/home-layout")
    public ResponseEntity<ResponseDto<?>> getAdminLayout() {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.getAdminLayout(), Result.ok()));
    }

    @PutMapping("/api/admin/home-layout/draft")
    public ResponseEntity<ResponseDto<?>> saveDraft(@RequestBody HomeLayoutContentDto dto, User user) {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.saveDraft(dto, user), Result.ok()));
    }

    @PostMapping("/api/admin/home-layout/publish")
    public ResponseEntity<ResponseDto<?>> publish(User user) {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.publish(user), Result.ok()));
    }

    @PostMapping("/api/admin/home-layout/reset")
    public ResponseEntity<ResponseDto<?>> resetDraft(User user) {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.resetDraft(user), Result.ok()));
    }

    @PostMapping("/api/admin/home-layout/media")
    public ResponseEntity<ResponseDto<?>> uploadMedia(@RequestParam("file") MultipartFile file, User user) {
        return ResponseEntity.ok(ResponseDto.of(homeLayoutService.uploadMedia(file), Result.ok()));
    }
}

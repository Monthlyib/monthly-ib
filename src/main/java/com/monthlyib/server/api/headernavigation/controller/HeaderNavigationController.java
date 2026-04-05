package com.monthlyib.server.api.headernavigation.controller;

import com.monthlyib.server.api.headernavigation.dto.HeaderNavigationConfigDto;
import com.monthlyib.server.domain.headernavigation.service.HeaderNavigationService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class HeaderNavigationController {

    private final HeaderNavigationService headerNavigationService;

    @GetMapping("/open-api/header-navigation")
    public ResponseEntity<ResponseDto<?>> getPublishedConfig() {
        return ResponseEntity.ok(ResponseDto.of(headerNavigationService.getPublishedConfig(), Result.ok()));
    }

    @GetMapping("/api/admin/header-navigation")
    public ResponseEntity<ResponseDto<?>> getAdminConfig(User user) {
        return ResponseEntity.ok(ResponseDto.of(headerNavigationService.getAdminConfig(user), Result.ok()));
    }

    @PutMapping("/api/admin/header-navigation")
    public ResponseEntity<ResponseDto<?>> saveConfig(@RequestBody HeaderNavigationConfigDto dto, User user) {
        return ResponseEntity.ok(ResponseDto.of(headerNavigationService.saveConfig(dto, user), Result.ok()));
    }
}

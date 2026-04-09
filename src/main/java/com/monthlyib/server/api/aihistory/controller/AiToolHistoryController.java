package com.monthlyib.server.api.aihistory.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.aihistory.dto.AiToolHistoryDetailResponseDto;
import com.monthlyib.server.api.aihistory.dto.AiToolHistorySummaryResponseDto;
import com.monthlyib.server.domain.aihistory.service.AiToolHistoryService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AiToolHistoryController {

    private final AiToolHistoryService aiToolHistoryService;

    @GetMapping("/api/ai-history/me")
    public ResponseEntity<PageResponseDto<?>> getMyHistory(
            @RequestParam(required = false) String toolType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @UserSession User user
    ) {
        Page<AiToolHistorySummaryResponseDto> response = aiToolHistoryService.findMyHistory(user, toolType, page, size);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @GetMapping("/api/ai-history/me/{historyId}")
    public ResponseEntity<ResponseDto<?>> getMyHistoryDetail(
            @PathVariable Long historyId,
            @UserSession User user
    ) {
        AiToolHistoryDetailResponseDto response = aiToolHistoryService.findMyHistoryDetail(historyId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @GetMapping("/api/admin/ai-history")
    public ResponseEntity<PageResponseDto<?>> getAdminHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) String toolType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @UserSession User user
    ) {
        Page<AiToolHistorySummaryResponseDto> response = aiToolHistoryService.findAdminHistory(userId, toolType, page, size, user);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @GetMapping("/api/admin/ai-history/{historyId}")
    public ResponseEntity<ResponseDto<?>> getAdminHistoryDetail(
            @PathVariable Long historyId,
            @UserSession User user
    ) {
        AiToolHistoryDetailResponseDto response = aiToolHistoryService.findAdminHistoryDetail(historyId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}

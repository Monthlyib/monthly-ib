package com.monthlyib.server.api.tutoring.controller;


import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.tutoring.dto.*;
import com.monthlyib.server.domain.tutoring.service.TutoringService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/tutoring")
public class TutoringController implements TutoringControllerIfs{

    private final TutoringService tutoringService;


    @Override
    @GetMapping("/date-simple")
    public ResponseEntity<ResponseDto<?>> getDateTutoring(TutoringSearchDto requestDto) {
        TutoringSimpleResponseDto response = tutoringService.findTutoringSimple(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/date")
    public ResponseEntity<ResponseDto<?>> getDateTutoringList(
            TutoringAdminSearchDto requestDto,
            @UserSession User user
    ) {
        TutoringDetailResponseDto response = tutoringService.findTutoringDetail(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/time")
    public ResponseEntity<ResponseDto<?>> getTimeTutoringList(TutoringTimeSearchDto requestDto) {
        List<TutoringResponseDto> response = tutoringService.findTimeTutoring(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDto<?>> getUserTutoringList(
            @PathVariable Long userId,
            TutoringUserSearchDto requestDto,
            @UserSession User user
    ) {
        return ResponseEntity.ok(PageResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping
    public ResponseEntity<ResponseDto<?>> postTutoring(
            @RequestBody TutoringPostRequestDto requestDto,
            @UserSession User user
    ) {
        TutoringResponseDto response = tutoringService.createTutoring(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/{tutoringId}")
    public ResponseEntity<ResponseDto<?>> patchTutoring(
            @PathVariable Long tutoringId,
            @RequestBody TutoringPatchRequestDto requestDto,
            @UserSession User user
    ) {
        TutoringResponseDto response = tutoringService.updateTutoring(requestDto, user, tutoringId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/{tutoringId}")
    public ResponseEntity<ResponseDto<?>> deleteNews(
            @PathVariable Long tutoringId,
            @UserSession User user
    ) {
        tutoringService.deleteTutoring(tutoringId, user);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping("/{tutoringId}/calendar-sync")
    public ResponseEntity<ResponseDto<?>> syncTutoringCalendar(
            @PathVariable Long tutoringId,
            @UserSession User user
    ) {
        TutoringResponseDto response = tutoringService.syncCalendar(tutoringId, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}

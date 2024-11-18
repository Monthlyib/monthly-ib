package com.monthlyib.server.api.tutoring.controller;


import com.monthlyib.server.api.tutoring.dto.*;
import com.monthlyib.server.domain.tutoring.service.TutoringService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<ResponseDto<?>> getDateTutoringList(TutoringAdminSearchDto requestDto, User user) {
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
    public ResponseEntity<PageResponseDto<?>> getUserTutoringList(Long userId, TutoringUserSearchDto requestDto, User user) {
        return ResponseEntity.ok(PageResponseDto.of(Result.ok()));
    }

    @Override
    @PostMapping
    public ResponseEntity<ResponseDto<?>> postTutoring(TutoringPostRequestDto requestDto, User user) {
        TutoringResponseDto response = tutoringService.createTutoring(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/{tutoringId}")
    public ResponseEntity<ResponseDto<?>> patchTutoring(Long tutoringId,TutoringPatchRequestDto requestDto, User user) {
        TutoringResponseDto response = tutoringService.updateTutoring(requestDto, user, tutoringId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/{tutoringId}")
    public ResponseEntity<ResponseDto<?>> deleteNews(Long tutoringId, User user) {
        tutoringService.deleteTutoring(tutoringId, user);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}

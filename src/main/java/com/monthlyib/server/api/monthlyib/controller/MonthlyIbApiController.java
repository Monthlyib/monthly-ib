package com.monthlyib.server.api.monthlyib.controller;


import com.monthlyib.server.api.monthlyib.dto.*;
import com.monthlyib.server.domain.montlyib.service.MonthlyIbService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.utils.StubUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class MonthlyIbApiController implements MonthlyIbApiControllerIfs{

    
    private final MonthlyIbService monthlyIbService;


    @Override
    @GetMapping("/open-api/monthly-ib/list")
    public ResponseEntity<PageResponseDto<?>> getMonthlyIb(
            int page,
            MonthlyIbSearchDto requestDto
    ) {
        Page<MonthlyIbSimpleResponseDto> response = monthlyIbService.findAllMonthlyIb(page, requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/api/monthly-ib/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> getMonthlyIb(
            Long monthlyIbId
    ) {
        MonthlyIbResponseDto response = monthlyIbService.findMonthlyIbById(monthlyIbId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib")
    public ResponseEntity<ResponseDto<?>> postMonthlyIb(
            MonthlyIbPostDto requestDto,
            User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createMonthlyIb(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib/monthly-ib-thumbnail/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> postMonthlyIbImage(
            Long monthlyIbId,
            MultipartFile[] multipartFile,
            User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createOrUpdateMonthlyIbThumbnail(monthlyIbId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib/monthly-ib-pdf/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> postMonthlyIbPdf(
            Long monthlyIbId,
            MultipartFile[] multipartFile,
            User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createOrUpdateMonthlyIbPdf(monthlyIbId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/monthly-ib/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> patchMonthlyIb(
            Long monthlyIbId,
            MonthlyIbPatchDto requestDto,
            User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.updateMonthlyIb(monthlyIbId, requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/monthly-ib/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> deleteMonthlyIb(
            Long monthlyIbId,
            User user
    ) {
        monthlyIbService.deleteMonthlyIb(monthlyIbId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}

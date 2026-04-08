package com.monthlyib.server.api.monthlyib.controller;


import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.monthlyib.dto.*;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.monthlyib.server.domain.montlyib.service.MonthlyIbService;
import com.monthlyib.server.domain.montlyib.service.MonthlyIbPdfRenderService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class MonthlyIbApiController implements MonthlyIbApiControllerIfs{

    
    private final MonthlyIbService monthlyIbService;
    private final MonthlyIbPdfRenderService monthlyIbPdfRenderService;
    private final FileService fileService;


    @Override
    @GetMapping("/open-api/monthly-ib/list")
    public ResponseEntity<PageResponseDto<?>> getMonthlyIb(
            @RequestParam(defaultValue = "0") int page,
            @ModelAttribute MonthlyIbSearchDto requestDto
    ) {
        Page<MonthlyIbSimpleResponseDto> response = monthlyIbService.findAllMonthlyIb(page, requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping({"/api/monthly-ib/{monthlyIbId}", "/open-api/monthly-ib/{monthlyIbId}"})
    public ResponseEntity<ResponseDto<?>> getMonthlyIb(
            @PathVariable Long monthlyIbId
    ) {
        MonthlyIbResponseDto response = monthlyIbService.findMonthlyIbById(monthlyIbId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib")
    public ResponseEntity<ResponseDto<?>> postMonthlyIb(
            @RequestBody MonthlyIbPostDto requestDto,
            @UserSession User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createMonthlyIb(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib/content-image")
    public ResponseEntity<ResponseDto<?>> uploadMonthlyIbContentImage(
            @RequestPart("image") MultipartFile image,
            @UserSession User user
    ) {
        MonthlyIbContentImageResponseDto response = monthlyIbService.uploadContentImage(image);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib/monthly-ib-thumbnail/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> postMonthlyIbImage(
            @PathVariable Long monthlyIbId,
            @RequestPart("image") MultipartFile[] multipartFile,
            @UserSession User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createOrUpdateMonthlyIbThumbnail(monthlyIbId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/monthly-ib/monthly-ib-pdf/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> postMonthlyIbPdf(
            @PathVariable Long monthlyIbId,
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.createOrUpdateMonthlyIbPdf(monthlyIbId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/monthly-ib/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> patchMonthlyIb(
            @PathVariable Long monthlyIbId,
            @RequestBody MonthlyIbPatchDto requestDto,
            @UserSession User user
    ) {
        MonthlyIbResponseDto response = monthlyIbService.updateMonthlyIb(monthlyIbId, requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/monthly-ib/{monthlyIbId}")
    public ResponseEntity<ResponseDto<?>> deleteMonthlyIb(
            @PathVariable Long monthlyIbId,
            @UserSession User user
    ) {
        monthlyIbService.deleteMonthlyIb(monthlyIbId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @GetMapping("/open-api/monthly-ib/{monthlyIbId}/pdf")
    public ResponseEntity<?> downloadMonthlyIbPdf(@PathVariable Long monthlyIbId) {
        MonthlyIb monthlyIb = monthlyIbService.findEntityById(monthlyIbId);
        List<MonthlyIbPdfFileResponseDto> pdfFiles = monthlyIbService.findPdfFiles(monthlyIbId);

        if (!monthlyIbService.hasMeaningfulContent(monthlyIb.getContent()) && !pdfFiles.isEmpty()) {
            MonthlyIbPdfFileResponseDto legacyPdf = pdfFiles.get(0);
            return buildLegacyPdfResponse(legacyPdf);
        }

        byte[] pdf = monthlyIbPdfRenderService.render(monthlyIb);
        String baseFileName = (monthlyIb.getTitle() == null || monthlyIb.getTitle().isBlank())
                ? "monthly-ib.pdf"
                : monthlyIb.getTitle() + ".pdf";

        HttpHeaders headers = fileService.buildHeaders(baseFileName, pdf);
        headers.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    private ResponseEntity<byte[]> buildLegacyPdfResponse(MonthlyIbPdfFileResponseDto legacyPdf) {
        try {
            byte[] pdfBytes = new URL(legacyPdf.getFileUrl()).openStream().readAllBytes();
            HttpHeaders headers = fileService.buildHeaders(legacyPdf.getFileName(), pdfBytes);
            headers.setContentType(MediaType.APPLICATION_PDF);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException exception) {
            log.error("Failed to download legacy monthly ib pdf: {}", legacyPdf.getFileUrl(), exception);
            throw new ServiceLogicException(ErrorCode.AWS_FILE_NOT_FOUND);
        }
    }
}

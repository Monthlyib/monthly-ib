package com.monthlyib.server.api.mail.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.mail.dto.AdminMailJobResponseDto;
import com.monthlyib.server.api.mail.dto.AdminMailPostDto;
import com.monthlyib.server.domain.mail.service.AdminMailJobService;
import com.monthlyib.server.domain.mail.service.AdminMailService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailApiController {

    private final AdminMailService adminMailService;
    private final AdminMailJobService adminMailJobService;

    @GetMapping("/jobs")
    public ResponseEntity<ResponseDto<?>> getMailJobs(@UserSession User user) {
        List<AdminMailJobResponseDto> response = adminMailJobService.findRecentJobs(user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @PostMapping
    public ResponseEntity<ResponseDto<?>> postMail(
            @RequestPart("request") AdminMailPostDto requestDto,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
            @RequestPart(value = "inlineImages", required = false) MultipartFile[] inlineImages,
            @UserSession User user
    ) {
        Map<String, Object> response = adminMailService.send(requestDto, attachments, inlineImages, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}

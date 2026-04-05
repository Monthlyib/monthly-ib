package com.monthlyib.server.api.mail.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.mail.dto.AdminMailPostDto;
import com.monthlyib.server.domain.mail.service.AdminMailService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailApiController {

    private final AdminMailService adminMailService;

    @PostMapping
    public ResponseEntity<ResponseDto<?>> postMail(
            @RequestBody AdminMailPostDto requestDto,
            @UserSession User user
    ) {
        Map<String, Object> response = adminMailService.send(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}

package com.monthlyib.server.api.user.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.user.dto.UserSocialReconcileResponseDto;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserApiController {

    private final UserService userService;

    @PostMapping("/reconcile-social-links")
    public ResponseEntity<ResponseDto<?>> reconcileSocialLinks(@UserSession User user) {
        UserSocialReconcileResponseDto res = userService.reconcileSocialLinks(user);
        return ResponseEntity.ok(ResponseDto.of(res, Result.ok()));
    }
}

package com.monthlyib.server.api.subscribe.controller;


import com.monthlyib.server.api.subscribe.dto.SubscribePostDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeResponseDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserPatchDto;
import com.monthlyib.server.api.subscribe.dto.SubscribeUserResponseDto;
import com.monthlyib.server.domain.subscribe.service.SubscribeService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class SubscribeApiController implements SubscribeApiControllerIfs{

    private final SubscribeService subscribeService;

    @Override
    @GetMapping("/open-api/subscribe")
    public ResponseEntity<ResponseDto<?>> getSubscribe() {
        List<SubscribeResponseDto> response = subscribeService.findAllSubscribe();
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/api/subscribe/{userId}")
    public ResponseEntity<PageResponseDto<?>> getSubscribeUser(int page, Long userId, User user) {
        Page<SubscribeUserResponseDto> response = subscribeService.findAllSubscribeUser(userId, page, user);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/api/subscribe/active/{userId}")
    public ResponseEntity<ResponseDto<SubscribeUserResponseDto>> getActiveSubscribeUser(Long userId, User user) {
        SubscribeUserResponseDto subscribeUserResponseDto = subscribeService.verifyActiveSubUser(userId);
        return ResponseEntity.ok(ResponseDto.of(subscribeUserResponseDto, Result.ok()));
    }

    @Override
    @PostMapping("/api/subscribe")
    public ResponseEntity<ResponseDto<?>> postSubscribe(SubscribePostDto requestDto, User user) {
        SubscribeResponseDto response = subscribeService.createSubscribe(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/subscribe/user/{userId}/{subscribeId}")
    public ResponseEntity<ResponseDto<?>> postSubscribeUser(Long subscribeId, Long userId, User user) {
        SubscribeUserResponseDto response = subscribeService.createSubscribeUser(subscribeId, user, userId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/subscribe/{subscribeId}")
    public ResponseEntity<ResponseDto<?>> patchSubscribe(Long subscribeId, SubscribePostDto requestDto, User user) {
        SubscribeResponseDto response = subscribeService.updateSubscribe(subscribeId, requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/subscribe/user/{subscribeUserId}")
    public ResponseEntity<ResponseDto<?>> patchSubscribeUser(Long subscribeUserId, SubscribeUserPatchDto requestDto, User user) {
        SubscribeUserResponseDto response = subscribeService.updateSubscribeUser(subscribeUserId, requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/subscribe/{subscribeId}")
    public ResponseEntity<ResponseDto<?>> deleteSubscribe(Long subscribeId, User user) {
        subscribeService.deleteSubscribe(subscribeId);
        return ResponseEntity.ok().build();
    }
}

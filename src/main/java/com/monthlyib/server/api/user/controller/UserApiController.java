package com.monthlyib.server.api.user.controller;

import com.monthlyib.server.api.user.dto.UserPatchRequestDto;
import com.monthlyib.server.api.user.dto.UserResponseDto;
import com.monthlyib.server.api.user.dto.UserSocialPatchRequestDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserApiController implements UserApiControllerIfs{

    private final UserService userService;

    @Override
    @GetMapping("/list")
    public ResponseEntity<PageResponseDto<?>> getUserList(int page,User user) {
        Page<UserResponseDto> response = userService.findAll(page, user);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseDto<?>> getUser(Long userId) {
        UserResponseDto res = userService.findUserById(userId);
        return ResponseEntity.ok(ResponseDto.of(res, Result.ok()));
    }

    @Override
    @PatchMapping("/{userId}")
    public ResponseEntity<ResponseDto<?>> patchUser(Long userId, UserPatchRequestDto requestDto, User user) {
        UserResponseDto res = userService.updateUser(userId, requestDto);
        return ResponseEntity.ok(ResponseDto.of(res, Result.ok()));
    }

    @Override
    @PatchMapping("/social/{userId}")
    public ResponseEntity<ResponseDto<?>> patchSocialUser(Long userId, UserSocialPatchRequestDto requestDto, User user) {
        UserResponseDto res = userService.updateSocialUser(userId, requestDto);
        return ResponseEntity.ok(ResponseDto.of(res, Result.ok()));
    }

    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseDto<?>> deleteUser(Long userId, User user) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }

    @Override
    @GetMapping("/verify/{username}")
    public ResponseEntity<ResponseDto<?>> verifyUser(String username, User user) {
        String findUsername = user.getUsername();
        if (username.equals(findUsername)) {
            return ResponseEntity.ok(ResponseDto.of(Map.of(
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "nickName", user.getNickName()
            ),Result.ok()));
        } else {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    @Override
    @PostMapping("/image/{userId}")
    public ResponseEntity<ResponseDto<?>> postUserImage(Long userId, MultipartFile[] multipartFile, User user) {
        UserResponseDto res = userService.createOrUpdateUserImage(userId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(res, Result.ok()));

    }
}

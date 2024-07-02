package com.monthlyib.server.api.videolessons.controller;

import com.amazonaws.services.dynamodbv2.xspec.L;
import com.monthlyib.server.api.videolessons.dto.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.videolessons.service.VideoLessonsService;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import com.monthlyib.server.utils.StubUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.Lint;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class VideoApiController implements VideoApiControllerIfs{

    private final VideoLessonsService videoLessonsService;

    @Override
    @GetMapping("/open-api/video")
    public ResponseEntity<PageResponseDto<?>> getVideoLessonsList(VideoLessonsSearchDto requestDto) {
        Page<VideoLessonsSimpleResponseDto> response = videoLessonsService.findAllSimple(requestDto);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @GetMapping("/open-api/video/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> getVideoLessons(int replyPage, Long videoLessonsId) {
        VideoLessonsResponseDto response = videoLessonsService.findVideoLessons(videoLessonsId, replyPage);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video")
    public ResponseEntity<ResponseDto<?>> postVideoLessons(VideoLessonsPostDto requestDto, User user) {
        VideoLessonsResponseDto response = videoLessonsService.createVideoLessons(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/api/video/enrolment/{userId}")
    public ResponseEntity<PageResponseDto<?>> getVideoLessonsForUser(Long userId, int page, User user) {
        Page<VideoLessonsUserSimpleResponseDto> response = videoLessonsService.findAllByUser(user, page, userId);
        return ResponseEntity.ok(PageResponseDto.of(response, response.getContent(), Result.ok()));
    }

    @Override
    @PostMapping("/api/video/enrolment/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> postVideoLessonsForUser(Long videoLessonsId, User user) {
        VideoLessonsResponseDto response = videoLessonsService.createVideoLessonsUser(user, videoLessonsId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video-image/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> postVideoLessonsImage(Long videoLessonsId, MultipartFile[] multipartFile, User user) {
        VideoLessonsResponseDto response = videoLessonsService.createOrUpdateVideoLessonsThumbnail(videoLessonsId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video-file/{chapterId}")
    @Hidden
    public ResponseEntity<ResponseDto<?>> postVideoFile(Long chapterId, MultipartFile[] multipartFile, User user) {
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping("/api/video")
    public ResponseEntity<ResponseDto<?>> patchVideo(VideoLessonsPatchDto requestDto, User user) {
        VideoLessonsResponseDto response = videoLessonsService.updateVideoLessons(requestDto);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/video/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> deleteVideoLessons(Long videoLessonsId, User user) {
        videoLessonsService.deleteVideoLessons(videoLessonsId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/api/video-reply")
    public ResponseEntity<ResponseDto<?>> postVideoLessonsReply(VideoLessonsReplyPostDto requestDto, User user) {
        VideoLessonsReplyResponseDto response = videoLessonsService.createVideoLessonsReply(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/video-reply")
    public ResponseEntity<ResponseDto<?>> patchVideoLessonsReply(VideoLessonsReplyPatchDto requestDto, User user) {
        VideoLessonsReplyResponseDto response = videoLessonsService.updateVideoLessonsReply(requestDto, user);

        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/video-reply/{videoLessonsReplyId}")
    public ResponseEntity<ResponseDto<?>> deleteVideoReply(Long videoLessonsReplyId, User user) {
        videoLessonsService.deleteVideoLessonsReply(videoLessonsReplyId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/api/video-reply/vote/{videoLessonsReplyId}")
    public ResponseEntity<ResponseDto<?>> voteVideoReply(Long videoLessonsReplyId, User user) {
        VideoLessonsReplyResponseDto response = videoLessonsService.voteReply(videoLessonsReplyId, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @GetMapping("/open-api/video-category")
    public ResponseEntity<ResponseDto<?>> getVideoCategory() {
        VideoCategoryDetailResponseDto response = videoLessonsService.findAllCategory();
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video-category")
    public ResponseEntity<ResponseDto<?>> postVideoLessonsCategory(VideoCategoryPostDto requestDto, User user) {
        VideoCategoryResponseDto response = videoLessonsService.createCategory(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/video-category")
    public ResponseEntity<ResponseDto<?>> patchVideoLessonsCategory(VideoCategoryPatchDto requestDto, User user) {
        VideoCategoryResponseDto response = videoLessonsService.updateCategory(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/video-category/{videoCategoryId}")
    public ResponseEntity<ResponseDto<?>> deleteVideoCategory(Long videoCategoryId, User user) {
        videoLessonsService.deleteCategory(videoCategoryId);
        return ResponseEntity.ok().build();
    }
}

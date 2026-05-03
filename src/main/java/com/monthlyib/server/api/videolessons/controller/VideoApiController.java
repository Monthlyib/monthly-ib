package com.monthlyib.server.api.videolessons.controller;

import com.monthlyib.server.api.videolessons.dto.*;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.videolessons.service.VideoLessonsService;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ResponseDto<?>> postVideoLessons(
            @RequestBody VideoLessonsPostDto requestDto,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsResponseDto response = videoLessonsService.createVideoLessons(requestDto, user);
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
    @GetMapping("/api/video/progress/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> getVideoLessonsProgress(
            @PathVariable Long videoLessonsId,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsProgressResponseDto response = videoLessonsService.findVideoLessonsProgress(user, videoLessonsId);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PutMapping("/api/video/progress/{videoLessonsId}/lesson/{subChapterId}")
    public ResponseEntity<ResponseDto<?>> putVideoLessonsProgress(
            @PathVariable Long videoLessonsId,
            @PathVariable Long subChapterId,
            @RequestBody VideoLessonsProgressUpsertDto requestDto,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsProgressResponseDto response = videoLessonsService.upsertVideoLessonsProgress(
                user,
                videoLessonsId,
                subChapterId,
                requestDto
        );
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video/progress/{videoLessonsId}/lesson/{subChapterId}/restart")
    public ResponseEntity<ResponseDto<?>> restartVideoLessonsProgress(
            @PathVariable Long videoLessonsId,
            @PathVariable Long subChapterId,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsProgressResponseDto response = videoLessonsService.restartVideoLessonsProgress(
                user,
                videoLessonsId,
                subChapterId
        );
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video-image/{videoLessonsId}")
    public ResponseEntity<ResponseDto<?>> postVideoLessonsImage(
            @PathVariable Long videoLessonsId,
            @RequestPart("image") MultipartFile[] multipartFile,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsResponseDto response = videoLessonsService.createOrUpdateVideoLessonsThumbnail(videoLessonsId, multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PostMapping("/api/video-file")
    @Hidden
    public ResponseEntity<ResponseDto<?>> postVideoFile(
            @RequestPart("file") MultipartFile multipartFile,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsMediaUploadResponseDto response = videoLessonsService.uploadVideoLessonFile(multipartFile, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/video")
    public ResponseEntity<ResponseDto<?>> patchVideo(
            @RequestBody VideoLessonsPatchDto requestDto,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsResponseDto response = videoLessonsService.updateVideoLessons(requestDto, user);
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
    public ResponseEntity<ResponseDto<?>> postVideoLessonsReply(
            @RequestBody VideoLessonsReplyPostDto requestDto,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsReplyResponseDto response = videoLessonsService.createVideoLessonsReply(requestDto, user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @PatchMapping("/api/video-reply")
    public ResponseEntity<ResponseDto<?>> patchVideoLessonsReply(
            @RequestBody VideoLessonsReplyPatchDto requestDto,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsReplyResponseDto response = videoLessonsService.updateVideoLessonsReply(requestDto, user);

        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @Override
    @DeleteMapping("/api/video-reply/{videoLessonsReplyId}")
    public ResponseEntity<ResponseDto<?>> deleteVideoReply(
            @PathVariable Long videoLessonsReplyId,
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        videoLessonsService.deleteVideoLessonsReply(videoLessonsReplyId, user);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/api/video-reply/vote/{videoLessonsReplyId}")
    public ResponseEntity<ResponseDto<?>> voteVideoReply(Long videoLessonsReplyId, User user) {
        VideoLessonsReplyResponseDto response = videoLessonsService.voteReply(videoLessonsReplyId, user.getUserId());
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @PostMapping("/api/admin/video-reply/deduplicate")
    public ResponseEntity<ResponseDto<?>> deduplicateVideoLessonsReviews(
            @com.monthlyib.server.annotation.UserSession User user
    ) {
        VideoLessonsReviewDeduplicateResponseDto response = videoLessonsService.deduplicateVideoLessonsReviews(user);
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

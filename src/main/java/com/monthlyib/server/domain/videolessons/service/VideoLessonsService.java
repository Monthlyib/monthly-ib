package com.monthlyib.server.domain.videolessons.service;

import com.monthlyib.server.api.videolessons.dto.*;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.VideoChapterStatus;
import com.monthlyib.server.constant.VideoLessonsUserStatus;
import com.monthlyib.server.domain.subscribe.service.SubscribeService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.service.UserService;
import com.monthlyib.server.domain.videolessons.entity.*;
import com.monthlyib.server.domain.videolessons.repository.VideoLessonsRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VideoLessonsService {

    private final VideoLessonsRepository videoLessonsRepository;

    private final FileService fileService;
    private final UserService userService;
    private final SubscribeService subscribeService;

    public Page<VideoLessonsSimpleResponseDto> findAllSimple(VideoLessonsSearchDto dto) {
        return videoLessonsRepository.findAll(
                PageRequest.of(dto.getPage(), 10, Sort.by("createAt").descending()),
                dto
        ).map(VideoLessonsSimpleResponseDto::of);
    }

    public VideoLessonsResponseDto findVideoLessons(Long videoLessonsId, int page) {
        VideoLessons findVideoLessons = verifyVideoLessons(videoLessonsId);
        List<VideoLessonsMainChapter> videoMainChapters = videoLessonsRepository.findVideoMainChapters(videoLessonsId);
        List<VideoLessonsChapterResponseDto> mainChapter = new ArrayList<>();
        videoMainChapters.forEach(c -> {
            List<VideoLessonsSubChapterResponseDto> sub = videoLessonsRepository.findVideoSubChaptersByMainChapterId(c.getVideoLessonsMainChapterId())
                    .stream().map(VideoLessonsSubChapterResponseDto::of).sorted().toList();
            mainChapter.add(VideoLessonsChapterResponseDto.of(c, sub));
        });

        Page<VideoLessonsReplyResponseDto> reply = videoLessonsRepository.findVideoReply(videoLessonsId, PageRequest.of(page, 10, Sort.by("createAt").descending()))
                .map(VideoLessonsReplyResponseDto::of);
        return VideoLessonsResponseDto.of(findVideoLessons, mainChapter, reply);
    }

    public VideoLessonsResponseDto createVideoLessons(VideoLessonsPostDto dto, User user) {
        verifyAdmin(user);
        List<VideoLessonsChapterPostDto> chapterPostDto = dto.getChapters();
        Long firstCategoryId = dto.getFirstCategoryId();
        Long secondCategoryId = dto.getSecondCategoryId();
        Long thirdCategoryId = dto.getThirdCategoryId();
        VideoLessonsCategory firstCategory = verifyVideoLessonsCategory(firstCategoryId);
        VideoLessonsCategory secondCategory = verifyVideoLessonsCategory(secondCategoryId);
        VideoLessonsCategory thirdCategory = verifyVideoLessonsCategory(thirdCategoryId);
        VideoLessons newVideo = VideoLessons.create(dto, firstCategory, secondCategory, thirdCategory);
        VideoLessons saveVideo = videoLessonsRepository.save(newVideo);
        Long videoLessonsId = saveVideo.getVideoLessonsId();
        chapterPostDto.stream().forEach(m -> {
            VideoLessonsMainChapter newMain = VideoLessonsMainChapter.create(videoLessonsId, m);
            VideoLessonsMainChapter saveMain = videoLessonsRepository.save(newMain);
            m.getSubChapters().stream().forEach( s -> {
                VideoLessonsSubChapter newSub = VideoLessonsSubChapter.create(videoLessonsId, saveMain.getVideoLessonsMainChapterId(), s);
                VideoLessonsSubChapter saveSub = videoLessonsRepository.save(newSub);
            });
        });
        return findVideoLessons(videoLessonsId, 0);
    }

    public VideoLessonsResponseDto updateVideoLessons(VideoLessonsPatchDto dto, User user) {
        verifyAdmin(user);
        List<VideoLessonsChapterPatchDto> chapterPatchDto = dto.getChapters();
        Long videoLessonsId = dto.getVideoLessonsId();
        Long firstCategoryId = dto.getFirstCategoryId();
        Long secondCategoryId = dto.getSecondCategoryId();
        Long thirdCategoryId = dto.getThirdCategoryId();
        VideoLessonsCategory firstCategory = verifyVideoLessonsCategory(firstCategoryId);
        VideoLessonsCategory secondCategory = verifyVideoLessonsCategory(secondCategoryId);
        VideoLessonsCategory thirdCategory = verifyVideoLessonsCategory(thirdCategoryId);
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        findVideo.update(dto, firstCategory, secondCategory, thirdCategory);
        VideoLessons saveVideo = videoLessonsRepository.save(findVideo);
        syncMainChapters(videoLessonsId, chapterPatchDto);
        return findVideoLessons(videoLessonsId, 0);
    }

    public VideoLessonsResponseDto createOrUpdateVideoLessonsThumbnail(Long videoLessonsId, MultipartFile[] files) {
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        List<VideoThumbnail> findImage = videoLessonsRepository.findVideoThumbnailByVideoLessonsId(videoLessonsId);
        if (!findImage.isEmpty()) {
            findImage.forEach(m -> {
                        fileService.deleteAwsFile(m.getFileName(), AwsProperty.VIDEO_LESSONS_THUMBNAIL);
                    }
            );
        }
        videoLessonsRepository.deleteAllVideoThumbnailByVideoLessonsId(videoLessonsId);
        for (MultipartFile multipartFile : files) {
            String url = fileService.saveMultipartFileForAws(multipartFile, AwsProperty.VIDEO_LESSONS_THUMBNAIL);
            String filename = multipartFile.getOriginalFilename();
            VideoThumbnail newFile = VideoThumbnail.create(findVideo.getVideoLessonsId(), filename, url);
            VideoThumbnail saveFile = videoLessonsRepository.save(newFile);
            findVideo.setVideoLessonsThumbnailId(saveFile.getVideoThumbnailId());
            findVideo.setVideoLessonsIbThumbnailUrl(saveFile.getUrl());
        }
        VideoLessons save = videoLessonsRepository.save(findVideo);
        return findVideoLessons(save.getVideoLessonsId(), 0);
    }

    public VideoLessonsMediaUploadResponseDto uploadVideoLessonFile(MultipartFile file, User user) {
        verifyAdmin(user);
        if (file == null || file.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.FILE_NOT_NULL);
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("video/")) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST);
        }

        String url = fileService.saveMultipartFileForAws(
                file,
                AwsProperty.STORAGE,
                "video-lessons/lesson/"
        );

        return VideoLessonsMediaUploadResponseDto.builder()
                .fileUrl(url)
                .fileName(file.getOriginalFilename())
                .contentType(contentType)
                .build();
    }

    public VideoLessonsReplyResponseDto createVideoLessonsReply(VideoLessonsReplyPostDto dto, User user) {
        verifyReviewUser(user);
        validateVideoLessonsReply(dto.getContent(), dto.getStar());
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        if (videoLessonsRepository.existsVideoLessonsReply(videoLessonsId, user.getUserId())) {
            throw new ServiceLogicException(ErrorCode.VIDEO_LESSONS_REPLY_ALREADY_EXISTS);
        }
        VideoLessonsReply newReply = VideoLessonsReply.create(dto, user);
        VideoLessonsReply saveReply = videoLessonsRepository.save(newReply);
        applyReviewSummary(
                findVideo,
                findVideo.getTotalStar() + saveReply.getStar(),
                videoLessonsRepository.countReply(videoLessonsId)
        );
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public VideoLessonsReplyResponseDto updateVideoLessonsReply(VideoLessonsReplyPatchDto dto, User user) {
        verifyReviewUser(user);
        validateVideoLessonsReply(dto.getContent(), dto.getStar());
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        VideoLessonsReply findReply = verifyVideoLessonsReply(dto.getVideoLessonsReplyId());
        verifyReviewOwnerOrAdmin(findReply, user);
        double previousStar = findReply.getStar();
        findReply.update(dto);
        VideoLessonsReply saveReply = videoLessonsRepository.save(findReply);
        applyReviewSummary(
                findVideo,
                findVideo.getTotalStar() - previousStar + saveReply.getStar(),
                videoLessonsRepository.countReply(videoLessonsId)
        );
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public void deleteVideoLessons(Long videoLessonsId) {
        videoLessonsRepository.deleteVideoLessons(verifyVideoLessons(videoLessonsId));
    }

    public void deleteVideoLessonsReply(Long videoLessonsReplyId, User user) {
        verifyReviewUser(user);
        VideoLessonsReply findReply = verifyVideoLessonsReply(videoLessonsReplyId);
        verifyReviewOwnerOrAdmin(findReply, user);
        VideoLessons findVideo = verifyVideoLessons(findReply.getVideoLessonsId());
        double nextTotalStar = findVideo.getTotalStar() - findReply.getStar();
        videoLessonsRepository.deleteVideoLessonsReply(videoLessonsReplyId);
        applyReviewSummary(
                findVideo,
                nextTotalStar,
                videoLessonsRepository.countReply(findVideo.getVideoLessonsId())
        );
        videoLessonsRepository.save(findVideo);
    }

    public VideoLessonsReplyResponseDto voteReply(Long videoLessonsReplyId, Long userId) {
        VideoLessonsReply findReply = verifyVideoLessonsReply(videoLessonsReplyId);
        User findUser = userService.findUserEntity(userId);
        Set<User> voter = findReply.getVoter();
        if (voter.contains(findUser)) {
            voter.remove(findUser);
        } else {
            voter.add(findUser);
        }
        findReply.setVoter(voter);
        VideoLessonsReply saveReply = videoLessonsRepository.save(findReply);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public VideoLessonsReviewDeduplicateResponseDto deduplicateVideoLessonsReviews(User user) {
        verifyAdmin(user);
        return deduplicateVideoLessonsReviews();
    }

    public VideoLessonsReviewDeduplicateResponseDto deduplicateVideoLessonsReviews() {
        List<VideoLessonsReply> allReplies = videoLessonsRepository.findAllVideoLessonsReplies();
        Map<String, List<VideoLessonsReply>> repliesByVideoAndAuthor = allReplies.stream()
                .filter(reply -> reply.getVideoLessonsId() != null && reply.getAuthorId() != null)
                .collect(Collectors.groupingBy(reply -> reply.getVideoLessonsId() + ":" + reply.getAuthorId()));

        int duplicateGroupCount = 0;
        int deletedReviewCount = 0;
        Set<Long> affectedVideoLessonsIds = new HashSet<>();

        for (List<VideoLessonsReply> duplicateGroup : repliesByVideoAndAuthor.values()) {
            if (duplicateGroup.size() <= 1) {
                continue;
            }

            duplicateGroupCount += 1;
            VideoLessonsReply canonicalReply = duplicateGroup.stream()
                    .max(Comparator
                            .comparing(this::reviewLastTouchedAt)
                            .thenComparing(reply -> Optional.ofNullable(reply.getVideoLessonsReplyId()).orElse(0L)))
                    .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR));

            affectedVideoLessonsIds.add(canonicalReply.getVideoLessonsId());
            duplicateGroup.stream()
                    .filter(reply -> !Objects.equals(reply.getVideoLessonsReplyId(), canonicalReply.getVideoLessonsReplyId()))
                    .forEach(reply -> videoLessonsRepository.deleteVideoLessonsReply(reply.getVideoLessonsReplyId()));
            deletedReviewCount += duplicateGroup.size() - 1;
        }

        affectedVideoLessonsIds.forEach(this::recalculateReviewSummary);

        return VideoLessonsReviewDeduplicateResponseDto.builder()
                .scannedReviewCount(allReplies.size())
                .duplicateGroupCount(duplicateGroupCount)
                .deletedReviewCount(deletedReviewCount)
                .updatedVideoLessonsCount(affectedVideoLessonsIds.size())
                .build();
    }

    public VideoCategoryDetailResponseDto findAllCategory() {
        return videoLessonsRepository.findAllCategory();
    }

    public VideoCategoryResponseDto createCategory(VideoCategoryPostDto dto, User user) {
        VideoLessonsCategory category = VideoLessonsCategory.create(dto);
        VideoLessonsCategory saveCategory = videoLessonsRepository.save(category);
        return VideoCategoryResponseDto.of(saveCategory);
    }

    public VideoCategoryResponseDto updateCategory(VideoCategoryPatchDto dto, User user) {
        Long videoCategoryId = dto.getVideoCategoryId();
        VideoLessonsCategory findCategory = verifyVideoLessonsCategory(videoCategoryId);
        VideoLessonsCategory category = findCategory.update(dto);
        VideoLessonsCategory saveCategory = videoLessonsRepository.save(category);
        return VideoCategoryResponseDto.of(saveCategory);
    }

    public void deleteCategory(Long videoCategoryId) {
        videoLessonsRepository.deleteVideoLessonsCategory(videoCategoryId);
    }

    public Page<VideoLessonsUserSimpleResponseDto> findAllByUser(User user, int page, Long userId) {
        Page<VideoLessonsUser> find = videoLessonsRepository.findAllVideoLessonsUser(userId, PageRequest.of(page, 10, Sort.by("createAt").descending()));
        return find.map(vu -> {
            VideoLessons videoLessons = verifyVideoLessons(vu.getVideoLessonsId());
            return VideoLessonsUserSimpleResponseDto.of(videoLessons, vu.getStatus());
        });
    }

    public VideoLessonsResponseDto createVideoLessonsUser(User user, Long videoLessonsId) {
        verifyVideoLessons(videoLessonsId);
        subscribeService.consumeCourseAccess(user, videoLessonsId);
        videoLessonsRepository.findVideoLessonsUser(videoLessonsId, user.getUserId())
                .orElseGet(() -> videoLessonsRepository.save(
                        VideoLessonsUser.builder()
                                .userId(user.getUserId())
                                .videoLessonsId(videoLessonsId)
                                .status(VideoLessonsUserStatus.PROGRESS)
                                .build()
                ));
        return findVideoLessons(videoLessonsId, 0);
    }

    public VideoLessonsProgressResponseDto findVideoLessonsProgress(User user, Long videoLessonsId) {
        verifyVideoLessons(videoLessonsId);
        subscribeService.ensureCourseAccessible(user, videoLessonsId);
        verifyVideoLessonsUser(videoLessonsId, user.getUserId());
        return buildProgressResponse(user.getUserId(), videoLessonsId);
    }

    public VideoLessonsProgressResponseDto upsertVideoLessonsProgress(
            User user,
            Long videoLessonsId,
            Long subChapterId,
            VideoLessonsProgressUpsertDto dto
    ) {
        verifyVideoLessons(videoLessonsId);
        subscribeService.ensureCourseAccessible(user, videoLessonsId);
        verifyVideoLessonsUser(videoLessonsId, user.getUserId());
        VideoLessonsSubChapter subChapter = verifyProgressTarget(videoLessonsId, subChapterId);

        long durationSeconds = normalizeToPositiveNumber(dto.getDurationSeconds());
        long lastPositionSeconds = normalizeToPositiveNumber(dto.getLastPositionSeconds());

        if (durationSeconds > 0 && lastPositionSeconds > durationSeconds) {
            lastPositionSeconds = durationSeconds;
        }

        double progressPercent = calculateLessonProgressPercent(lastPositionSeconds, durationSeconds);
        boolean completed = progressPercent >= 90D;
        long normalizedDurationSeconds = durationSeconds;
        long normalizedLastPositionSeconds = lastPositionSeconds;
        double normalizedProgressPercent = progressPercent;
        boolean normalizedCompleted = completed;

        VideoLessonsProgress progress = videoLessonsRepository
                .findVideoLessonsProgress(user.getUserId(), videoLessonsId, subChapterId)
                .orElseGet(() -> VideoLessonsProgress.create(
                        user.getUserId(),
                        videoLessonsId,
                        subChapter.getMainChapterId(),
                        subChapterId,
                        normalizedLastPositionSeconds,
                        normalizedDurationSeconds,
                        normalizedProgressPercent,
                        normalizedCompleted
                ));

        progress.updateProgress(
                subChapter.getMainChapterId(),
                normalizedLastPositionSeconds,
                normalizedDurationSeconds,
                normalizedProgressPercent,
                normalizedCompleted
        );
        videoLessonsRepository.save(progress);

        return buildProgressResponse(user.getUserId(), videoLessonsId);
    }

    public VideoLessonsProgressResponseDto restartVideoLessonsProgress(User user, Long videoLessonsId, Long subChapterId) {
        verifyVideoLessons(videoLessonsId);
        subscribeService.ensureCourseAccessible(user, videoLessonsId);
        verifyVideoLessonsUser(videoLessonsId, user.getUserId());
        VideoLessonsSubChapter subChapter = verifyProgressTarget(videoLessonsId, subChapterId);

        VideoLessonsProgress progress = videoLessonsRepository
                .findVideoLessonsProgress(user.getUserId(), videoLessonsId, subChapterId)
                .orElseGet(() -> VideoLessonsProgress.create(
                        user.getUserId(),
                        videoLessonsId,
                        subChapter.getMainChapterId(),
                        subChapterId,
                        0L,
                        0L,
                        0D,
                        false
                ));
        progress.restart();
        videoLessonsRepository.save(progress);

        return buildProgressResponse(user.getUserId(), videoLessonsId);
    }



    private VideoLessonsReply verifyVideoLessonsReply(Long videoLessonsReplyId) {
        return videoLessonsRepository.findVideoReplyById(videoLessonsReplyId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS));

    }

    private VideoLessons verifyVideoLessons(Long videoLessonsId) {
        return videoLessonsRepository.findVideoById(videoLessonsId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS));

    }

    private VideoLessonsCategory verifyVideoLessonsCategory(Long videoLessonsCategoryId) {
        return videoLessonsRepository.findVideoCategory(videoLessonsCategoryId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_CATEGORY));

    }

    private VideoLessonsMainChapter verifyVideoLessonsMainChapter(Long videoLessonsMainChapterId) {
        return videoLessonsRepository.findVideoMainChapter(videoLessonsMainChapterId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_MAIN_CHAPTER));
    }

    private VideoLessonsSubChapter verifyVideoLessonsSubChapter(Long videoLessonsSubChapterId) {
        return videoLessonsRepository.findVideoSubChapter(videoLessonsSubChapterId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_SUB_CHAPTER));
    }

    private VideoLessonsUser verifyVideoLessonsUser(Long videoLessonsId, Long userId) {
        return videoLessonsRepository.findVideoLessonsUser(videoLessonsId, userId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.ACCESS_DENIED_REQUEST_API));
    }

    private void syncMainChapters(Long videoLessonsId, List<VideoLessonsChapterPatchDto> requestChapters) {
        List<VideoLessonsChapterPatchDto> normalizedChapters = requestChapters == null
                ? List.of()
                : requestChapters.stream()
                .sorted(Comparator.comparingInt(VideoLessonsChapterPatchDto::getChapterIndex))
                .toList();

        List<VideoLessonsMainChapter> existingMainChapters = videoLessonsRepository.findVideoMainChapters(videoLessonsId);
        Set<Long> requestedMainChapterIds = normalizedChapters.stream()
                .map(VideoLessonsChapterPatchDto::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingMainChapters.stream()
                .filter(chapter -> !requestedMainChapterIds.contains(chapter.getVideoLessonsMainChapterId()))
                .forEach(chapter -> deleteMainChapter(chapter.getVideoLessonsMainChapterId()));

        normalizedChapters.forEach(chapterDto -> {
            VideoLessonsMainChapter mainChapter = saveMainChapter(videoLessonsId, chapterDto);
            syncSubChapters(videoLessonsId, mainChapter.getVideoLessonsMainChapterId(), chapterDto.getSubChapters());
        });
    }

    private VideoLessonsMainChapter saveMainChapter(Long videoLessonsId, VideoLessonsChapterPatchDto dto) {
        if (dto.getChapterId() == null) {
            return videoLessonsRepository.save(
                    VideoLessonsMainChapter.builder()
                            .videoLessonsId(videoLessonsId)
                            .chapterStatus(dto.getChapterStatus() == null ? VideoChapterStatus.MAIN_CHAPTER : dto.getChapterStatus())
                            .chapterTitle(dto.getChapterTitle())
                            .chapterIndex(dto.getChapterIndex())
                            .build()
            );
        }

        VideoLessonsMainChapter mainChapter = verifyVideoLessonsMainChapter(dto.getChapterId());
        if (!videoLessonsId.equals(mainChapter.getVideoLessonsId())) {
            throw new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_MAIN_CHAPTER);
        }
        mainChapter.update(dto);
        return videoLessonsRepository.save(mainChapter);
    }

    private void syncSubChapters(Long videoLessonsId, Long mainChapterId, List<VideoLessonsSubChapterPatchDto> requestSubChapters) {
        List<VideoLessonsSubChapterPatchDto> normalizedSubChapters = requestSubChapters == null
                ? List.of()
                : requestSubChapters.stream()
                .sorted(Comparator.comparingInt(VideoLessonsSubChapterPatchDto::getChapterIndex))
                .toList();

        List<VideoLessonsSubChapter> existingSubChapters = videoLessonsRepository.findVideoSubChaptersByMainChapterId(mainChapterId);
        Set<Long> requestedSubChapterIds = normalizedSubChapters.stream()
                .map(VideoLessonsSubChapterPatchDto::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingSubChapters.stream()
                .filter(subChapter -> !requestedSubChapterIds.contains(subChapter.getVideoLessonsSubChapterId()))
                .forEach(subChapter -> videoLessonsRepository.deleteVideoLessonsSubChapter(subChapter.getVideoLessonsSubChapterId()));

        normalizedSubChapters.forEach(subChapterDto -> saveSubChapter(videoLessonsId, mainChapterId, subChapterDto));
    }

    private VideoLessonsSubChapter saveSubChapter(
            Long videoLessonsId,
            Long mainChapterId,
            VideoLessonsSubChapterPatchDto dto
    ) {
        if (dto.getChapterId() == null) {
            return videoLessonsRepository.save(
                    VideoLessonsSubChapter.builder()
                            .mainChapterId(mainChapterId)
                            .videoLessonsId(videoLessonsId)
                            .chapterStatus(dto.getChapterStatus() == null ? VideoChapterStatus.SUB_CHAPTER : dto.getChapterStatus())
                            .chapterTitle(dto.getChapterTitle())
                            .chapterIndex(dto.getChapterIndex())
                            .videoFileUrl(dto.getVideoFileUrl())
                            .build()
            );
        }

        VideoLessonsSubChapter subChapter = verifyVideoLessonsSubChapter(dto.getChapterId());
        if (!videoLessonsId.equals(subChapter.getVideoLessonsId()) || !mainChapterId.equals(subChapter.getMainChapterId())) {
            throw new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_SUB_CHAPTER);
        }
        subChapter.update(dto);
        return videoLessonsRepository.save(subChapter);
    }

    private void deleteMainChapter(Long mainChapterId) {
        videoLessonsRepository.deleteAllVideoLessonsSubChapterByMainChapterId(mainChapterId);
        videoLessonsRepository.deleteVideoLessonsMainChapter(mainChapterId);
    }

    private void verifyReviewUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void verifyReviewOwnerOrAdmin(VideoLessonsReply reply, User user) {
        if (user.getAuthority() == Authority.ADMIN || Objects.equals(reply.getAuthorId(), user.getUserId())) {
            return;
        }
        throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
    }

    private void validateVideoLessonsReply(String content, double star) {
        if (!StringUtils.hasText(content)) {
            throw new ServiceLogicException(ErrorCode.VIDEO_LESSONS_REPLY_CONTENT_REQUIRED);
        }
        if (star < 1D || star > 5D) {
            throw new ServiceLogicException(ErrorCode.VIDEO_LESSONS_REPLY_STAR_INVALID);
        }
    }

    private void applyReviewSummary(VideoLessons videoLessons, double totalStar, long replyCount) {
        double safeTotalStar = Math.max(0D, totalStar);
        videoLessons.setTotalStar(safeTotalStar);
        videoLessons.setReplyCount(Math.max(0L, replyCount));
        videoLessons.setStarAverage(replyCount <= 0 ? 0D : safeTotalStar / replyCount);
    }

    private LocalDateTime reviewLastTouchedAt(VideoLessonsReply reply) {
        if (reply.getUpdateAt() != null) {
            return reply.getUpdateAt();
        }
        if (reply.getCreateAt() != null) {
            return reply.getCreateAt();
        }
        return LocalDateTime.MIN;
    }

    private void recalculateReviewSummary(Long videoLessonsId) {
        VideoLessons videoLessons = verifyVideoLessons(videoLessonsId);
        List<VideoLessonsReply> replies = videoLessonsRepository.findAllVideoLessonsRepliesByVideoLessonsId(videoLessonsId);
        double totalStar = replies.stream()
                .mapToDouble(VideoLessonsReply::getStar)
                .sum();
        applyReviewSummary(videoLessons, totalStar, replies.size());
        videoLessonsRepository.save(videoLessons);
    }

    private void verifyAdmin(User user) {
        if (user == null || user.getAuthority() != Authority.ADMIN) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private VideoLessonsSubChapter verifyProgressTarget(Long videoLessonsId, Long subChapterId) {
        VideoLessonsSubChapter subChapter = verifyVideoLessonsSubChapter(subChapterId);
        if (!videoLessonsId.equals(subChapter.getVideoLessonsId())) {
            throw new ServiceLogicException(ErrorCode.NOT_FOUND_VIDEO_LESSONS_SUB_CHAPTER);
        }
        return subChapter;
    }

    private VideoLessonsProgressResponseDto buildProgressResponse(Long userId, Long videoLessonsId) {
        List<VideoLessonsSubChapter> subChapters = videoLessonsRepository.findVideoSubChaptersByVideoLessonsId(videoLessonsId)
                .stream()
                .sorted(Comparator
                        .comparingLong(VideoLessonsSubChapter::getMainChapterId)
                        .thenComparingInt(VideoLessonsSubChapter::getChapterIndex))
                .toList();

        List<VideoLessonsProgress> progressList = videoLessonsRepository.findAllVideoLessonsProgress(userId, videoLessonsId);
        Map<Long, VideoLessonsProgress> progressMap = progressList.stream().collect(
                Collectors.toMap(VideoLessonsProgress::getSubChapterId, Function.identity())
        );

        long totalLessonCount = subChapters.size();
        long completedLessonCount = progressList.stream().filter(VideoLessonsProgress::isCompleted).count();
        double totalProgress = subChapters.stream()
                .map(VideoLessonsSubChapter::getVideoLessonsSubChapterId)
                .map(progressMap::get)
                .filter(Objects::nonNull)
                .mapToDouble(VideoLessonsProgress::getProgressPercent)
                .sum();
        double progressPercent = totalLessonCount == 0
                ? 0D
                : roundToOneDecimal(totalProgress / totalLessonCount);

        VideoLessonsProgress resumeProgress = progressList.stream()
                .max(Comparator.comparing(VideoLessonsProgress::getLastWatchedAt))
                .orElse(null);

        List<VideoLessonsLessonProgressDto> lessons = subChapters.stream()
                .map(subChapter -> progressMap.get(subChapter.getVideoLessonsSubChapterId()))
                .filter(Objects::nonNull)
                .map(VideoLessonsLessonProgressDto::of)
                .toList();

        return VideoLessonsProgressResponseDto.builder()
                .videoLessonsId(videoLessonsId)
                .progressPercent(progressPercent)
                .completedLessonCount(completedLessonCount)
                .totalLessonCount(totalLessonCount)
                .resumeTarget(resumeProgress == null ? null : VideoLessonsProgressSummaryDto.builder()
                        .mainChapterId(resumeProgress.getMainChapterId())
                        .subChapterId(resumeProgress.getSubChapterId())
                        .positionSeconds(resumeProgress.getLastPositionSeconds())
                        .build())
                .lessons(lessons)
                .build();
    }

    private long normalizeToPositiveNumber(Long value) {
        if (value == null || value < 0) {
            return 0L;
        }
        return value;
    }

    private double calculateLessonProgressPercent(long lastPositionSeconds, long durationSeconds) {
        if (durationSeconds <= 0L) {
            return 0D;
        }
        double rawPercent = ((double) lastPositionSeconds / durationSeconds) * 100D;
        return roundToOneDecimal(Math.min(rawPercent, 100D));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10D) / 10D;
    }


}

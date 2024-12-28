package com.monthlyib.server.domain.videolessons.service;

import com.monthlyib.server.api.videolessons.dto.*;
import com.monthlyib.server.constant.AwsProperty;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.SubscribeStatus;
import com.monthlyib.server.constant.VideoLessonsUserStatus;
import com.monthlyib.server.domain.subscribe.entity.SubscribeUser;
import com.monthlyib.server.domain.subscribe.repository.SubscribeRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VideoLessonsService {

    private final VideoLessonsRepository videoLessonsRepository;

    private final FileService fileService;
    private final UserService userService;
    private final SubscribeRepository subscribeRepository;

    public Page<VideoLessonsSimpleResponseDto> findAllSimple(VideoLessonsSearchDto dto) {
        return videoLessonsRepository.findAll(
                PageRequest.of(dto.getPage(), 5, Sort.by("createAt").descending()),
                dto).map(VideoLessonsSimpleResponseDto::of);
    }

    public VideoLessonsResponseDto findVideoLessons(Long videoLessonsId, int page) {
    
        // VideoLesson 데이터를 가져옴
        VideoLessons findVideoLessons = verifyVideoLessons(videoLessonsId);
    
        // 메인 챕터와 서브 챕터 데이터 가져오기
        List<VideoLessonsMainChapter> videoMainChapters = videoLessonsRepository.findVideoMainChapters(videoLessonsId);
        List<VideoLessonsChapterResponseDto> mainChapter = new ArrayList<>();
        videoMainChapters.forEach(c -> {
            List<VideoLessonsSubChapterResponseDto> sub = videoLessonsRepository
                    .findVideoSubChaptersByMainChapterId(c.getVideoLessonsMainChapterId())
                    .stream().map(VideoLessonsSubChapterResponseDto::of).sorted().toList();
            mainChapter.add(VideoLessonsChapterResponseDto.of(c, sub));
        });
    
        // 수강후기 데이터 가져오기
        Page<VideoLessonsReplyResponseDto> reply = videoLessonsRepository
                .findVideoReply(videoLessonsId, PageRequest.of(page, 10, Sort.by("createAt").descending()))
                .map(VideoLessonsReplyResponseDto::of);
    
        // 응답 생성
        return VideoLessonsResponseDto.of(findVideoLessons, mainChapter, reply);
    }

    // 비디오 강의 및 챕터, 서브 챕터 데이터를 생성하고 저장
    public VideoLessonsResponseDto createVideoLessons(VideoLessonsPostDto dto) {
        // 비디오 강의 생성 요청 데이터에서 챕터 리스트와 카테고리 ID 가져오기
        List<VideoLessonsChapterPostDto> chapterPostDto = dto.getChapters();
        Long firstCategoryId = dto.getFirstCategoryId();
        Long secondCategoryId = dto.getSecondCategoryId();
        Long thirdCategoryId = dto.getThirdCategoryId();

        // 각 카테고리 ID를 기반으로 카테고리 존재 여부 확인 및 반환
        VideoLessonsCategory firstCategory = verifyVideoLessonsCategory(firstCategoryId);
        VideoLessonsCategory secondCategory = verifyVideoLessonsCategory(secondCategoryId);
        VideoLessonsCategory thirdCategory = verifyVideoLessonsCategory(thirdCategoryId);

        // DTO와 카테고리 정보를 기반으로 새 비디오 강의 생성
        VideoLessons newVideo = VideoLessons.create(dto, firstCategory, secondCategory, thirdCategory);

        // 생성된 비디오 강의를 저장하고 ID 반환
        VideoLessons saveVideo = videoLessonsRepository.save(newVideo);
        Long videoLessonsId = saveVideo.getVideoLessonsId();

        // 메인 챕터와 서브 챕터 생성 및 저장
        chapterPostDto.stream().forEach(m -> {
            // 비디오 강의 ID와 챕터 정보를 기반으로 메인 챕터 생성
            VideoLessonsMainChapter newMain = VideoLessonsMainChapter.create(videoLessonsId, m);
            VideoLessonsMainChapter saveMain = videoLessonsRepository.save(newMain);

            // 메인 챕터에 속한 서브 챕터 생성 및 저장
            m.getSubChapters().stream().forEach(s -> {
                VideoLessonsSubChapter newSub = VideoLessonsSubChapter.create(videoLessonsId,
                        saveMain.getVideoLessonsMainChapterId(), s);
                VideoLessonsSubChapter saveSub = videoLessonsRepository.save(newSub);
            });
        });

        // 생성된 비디오 강의와 챕터 정보를 포함한 응답 반환
        return findVideoLessons(videoLessonsId, 0);
    }

    // 비디오 강의 및 챕터, 서브 챕터 데이터를 업데이트하고 저장
    public VideoLessonsResponseDto updateVideoLessons(VideoLessonsPatchDto dto) {
        // 비디오 강의 업데이트 요청 데이터에서 챕터 리스트와 ID, 카테고리 ID 가져오기
        List<VideoLessonsChapterPatchDto> chapterPatchDto = dto.getChapters();
        Long videoLessonsId = dto.getVideoLessonsId();
        Long firstCategoryId = dto.getFirstCategoryId();
        Long secondCategoryId = dto.getSecondCategoryId();
        Long thirdCategoryId = dto.getThirdCategoryId();

        // 각 카테고리 ID를 기반으로 카테고리 존재 여부 확인 및 반환
        VideoLessonsCategory firstCategory = verifyVideoLessonsCategory(firstCategoryId);
        VideoLessonsCategory secondCategory = verifyVideoLessonsCategory(secondCategoryId);
        VideoLessonsCategory thirdCategory = verifyVideoLessonsCategory(thirdCategoryId);

        // 강의 ID를 기반으로 기존 비디오 강의 조회 및 검증
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);

        // 비디오 강의 정보 업데이트
        findVideo.update(dto, firstCategory, secondCategory, thirdCategory);
        VideoLessons saveVideo = videoLessonsRepository.save(findVideo);

        // 요청으로 들어온 메인 챕터 및 서브 챕터 ID 목록 수집
        Set<Long> requestedMainChapterIds = new HashSet<>();
        Map<Long, Set<Long>> requestedSubChapterIdsByMain = new HashMap<Long, Set<Long>>();

        chapterPatchDto.forEach(m -> {
            requestedMainChapterIds.add(m.getChapterId());
            Set<Long> subChapterIds = new HashSet<>();
            m.getSubChapters().forEach(s -> {
                if (s.getChapterId() != null) {
                    subChapterIds.add(s.getChapterId());
                }
            });
            requestedSubChapterIdsByMain.put(m.getChapterId(), subChapterIds);
        });

        // 데이터베이스에 존재하는 메인 챕터 및 서브 챕터 조회
        List<VideoLessonsMainChapter> existingMainChapters = videoLessonsRepository
                .findVideoMainChapters(videoLessonsId);

        // 메인 챕터와 서브 챕터를 Map으로 저장하여 로그에 출력
        Map<Long, List<VideoLessonsSubChapter>> existingSubChaptersByMain = existingMainChapters.stream()
                .collect(Collectors.toMap(
                        VideoLessonsMainChapter::getVideoLessonsMainChapterId,
                        mainChapter -> videoLessonsRepository
                                .findVideoSubChaptersByMainChapterId(mainChapter.getVideoLessonsMainChapterId())));

        // 기존 데이터베이스에 있는 메인 챕터 중 dto에 없는 메인 챕터 ID 삭제
        existingMainChapters.forEach(existingMainChapter -> {
            Long mainChapterId = existingMainChapter.getVideoLessonsMainChapterId();
            if (!requestedMainChapterIds.contains(mainChapterId)) {
                videoLessonsRepository.deleteVideoLessonsMainChapter(mainChapterId);
            }
        });

        // 메인 챕터별로 존재하는 서브 챕터 중 dto에 없는 서브 챕터 ID 삭제
        existingSubChaptersByMain.forEach((mainChapterId, existingSubChapters) -> {
            Set<Long> requestedSubChapterIds = requestedSubChapterIdsByMain.getOrDefault(mainChapterId,
                    Collections.emptySet());
            log.info("mainChapterId: {}, existingSubChapters: {}, requestedSubChapterIds: {}", mainChapterId,
                    existingSubChapters.stream().map(VideoLessonsSubChapter::getVideoLessonsSubChapterId).toList(),
                    requestedSubChapterIds);
            existingSubChapters.forEach(subChapter -> {
                if (!requestedSubChapterIds.contains(subChapter.getVideoLessonsSubChapterId())) {
                    videoLessonsRepository.deleteVideoLessonsSubChapter(subChapter.getVideoLessonsSubChapterId());
                }
            });
        });

        // 메인 챕터 및 서브 챕터 업데이트 또는 생성
        chapterPatchDto.forEach(m -> {
            VideoLessonsMainChapter saveMain;
            if (m.getChapterId() != null) {
                VideoLessonsMainChapter main = verifyVideoLessonsMainChapter(m.getChapterId());
                main.update(m);
                saveMain = videoLessonsRepository.save(main);
            } else {
                // Convert VideoLessonsChapterPatchDto to VideoLessonsChapterPostDto
                VideoLessonsChapterPostDto newMainDto = new VideoLessonsChapterPostDto(
                        m.getChapterStatus(),
                        m.getChapterTitle(),
                        m.getChapterIndex(),
                        m.getSubChapters().stream()
                                .map(sub -> new VideoLessonsSubChapterPostDto(
                                        sub.getChapterStatus(),
                                        sub.getChapterTitle(),
                                        sub.getChapterIndex(),
                                        sub.getVideoFileUrl()))
                                .collect(Collectors.toList()));

                VideoLessonsMainChapter newMain = VideoLessonsMainChapter.create(videoLessonsId, newMainDto);
                saveMain = videoLessonsRepository.save(newMain);
            }
            m.getSubChapters().forEach(s -> {
                if (s.getChapterId() != null) {
                    VideoLessonsSubChapter sub = verifyVideoLessonsSubChapter(s.getChapterId());
                    sub.update(s);
                    videoLessonsRepository.save(sub);
                } else {
                    VideoLessonsSubChapterPostDto newSubDto = VideoLessonsSubChapterPostDto.builder()
                            .chapterStatus(s.getChapterStatus())
                            .chapterTitle(s.getChapterTitle())
                            .chapterIndex(s.getChapterIndex())
                            .videoFileUrl(s.getVideoFileUrl())
                            .build();

                    VideoLessonsSubChapter newSub = VideoLessonsSubChapter.create(
                            videoLessonsId,
                            saveMain.getVideoLessonsMainChapterId(),
                            newSubDto);
                    videoLessonsRepository.save(newSub);
                }
            });
        });

        // 업데이트된 비디오 강의와 챕터 정보를 포함한 응답 반환
        return findVideoLessons(videoLessonsId, 0);
    }

    public VideoLessonsResponseDto createOrUpdateVideoLessonsThumbnail(Long videoLessonsId, MultipartFile[] files) {
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        List<VideoThumbnail> findImage = videoLessonsRepository.findVideoThumbnailByVideoLessonsId(videoLessonsId);
        if (!findImage.isEmpty()) {
            findImage.forEach(m -> {
                fileService.deleteAwsFile(m.getFileName(), AwsProperty.VIDEO_LESSONS_THUMBNAIL);
            });
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

    public VideoLessonsReplyResponseDto createVideoLessonsReply(VideoLessonsReplyPostDto dto, User user) {
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        VideoLessonsReply newReply = VideoLessonsReply.create(dto, user);
        VideoLessonsReply saveReply = videoLessonsRepository.save(newReply);
        findVideo.setReplyCount(videoLessonsRepository.countReply(videoLessonsId));
        findVideo.setTotalStar(findVideo.getTotalStar() + saveReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar() / findVideo.getReplyCount());
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public VideoLessonsReplyResponseDto updateVideoLessonsReply(VideoLessonsReplyPatchDto dto, User user) {
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        VideoLessonsReply findReply = verifyVideoLessonsReply(dto.getVideoLessonsReplyId());
        findReply.update(dto);
        VideoLessonsReply saveReply = videoLessonsRepository.save(findReply);
        findVideo.setTotalStar(findVideo.getTotalStar() - findReply.getStar());
        findVideo.setTotalStar(findVideo.getTotalStar() + saveReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar() / findVideo.getReplyCount());
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public void deleteVideoLessons(Long videoLessonsId) {
        videoLessonsRepository.deleteVideoLessons(verifyVideoLessons(videoLessonsId));
    }

    public void deleteVideoLessonsReply(Long videoLessonsReplyId) {
        VideoLessonsReply findReply = verifyVideoLessonsReply(videoLessonsReplyId);
        VideoLessons findVideo = verifyVideoLessons(findReply.getVideoLessonsId());
        findVideo.setTotalStar(findVideo.getTotalStar() - findReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar() / findVideo.getReplyCount());
        videoLessonsRepository.save(findVideo);
        videoLessonsRepository.deleteVideoLessonsReply(videoLessonsReplyId);
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
        Page<VideoLessonsUser> find = videoLessonsRepository.findAllVideoLessonsUser(userId,
                PageRequest.of(page, 5, Sort.by("createAt").descending()));
        return find.map(vu -> {
            VideoLessons videoLessons = verifyVideoLessons(vu.getVideoLessonsId());
            return VideoLessonsUserSimpleResponseDto.of(videoLessons, vu.getStatus());
        });
    }

    public VideoLessonsResponseDto createVideoLessonsUser(User user, Long videoLessonsId) {
        SubscribeUser findSubUser = subscribeRepository
                .findSubscribeUserByUserIdAndStatus(user.getUserId(), SubscribeStatus.ACTIVE)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
    
        // 현재 요청된 videoLessonsId가 사용자가 이미 수강 중인지 확인
        if (findSubUser.getVideoLessonsIdList().contains(videoLessonsId)) {
            log.info("User is already enrolled in video lesson ID: {}", videoLessonsId);
            return findVideoLessons(videoLessonsId, 0); // 이미 수강 중인 경우 정상 상태로 종료
        }
    
        if (findSubUser.getVideoLessonsCount() > 0) {
            log.info("Before saving - videoLessonsCount: {}", findSubUser.getVideoLessonsCount());
            findSubUser.setVideoLessonsCount(findSubUser.getVideoLessonsCount() - 1);
    
            List<Long> videoLessonsIdList = new ArrayList<>(findSubUser.getVideoLessonsIdList());
            videoLessonsIdList.add(videoLessonsId);
            findSubUser.setVideoLessonsIdList(videoLessonsIdList);
    
            log.info("Attempting to save SubscribeUser: {}", findSubUser);
            SubscribeUser savedSubUser = subscribeRepository.saveSubscribeUser(findSubUser);
            log.info("After saving SubscribeUser: {}", savedSubUser);
        } else {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    
        VideoLessonsUser newVideoLessonsUser = VideoLessonsUser.builder()
                .userId(user.getUserId())
                .videoLessonsId(videoLessonsId)
                .status(VideoLessonsUserStatus.PROGRESS)
                .build();
    
        VideoLessonsUser saveVideoLessonsUser = videoLessonsRepository.save(newVideoLessonsUser);
        return findVideoLessons(videoLessonsId, 0);
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

}

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
import java.util.List;
import java.util.Set;

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

    public VideoLessonsResponseDto createVideoLessons(VideoLessonsPostDto dto) {
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

    public VideoLessonsResponseDto updateVideoLessons(VideoLessonsPatchDto dto) {
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
        chapterPatchDto.stream().forEach(m -> {
            VideoLessonsMainChapter main = verifyVideoLessonsMainChapter(m.getChapterId());
            main.update(m);
            VideoLessonsMainChapter saveMain = videoLessonsRepository.save(main);
            m.getSubChapters().stream().forEach( s -> {
                VideoLessonsSubChapter sub = verifyVideoLessonsSubChapter(s.getChapterId());
                sub.update(s);
                VideoLessonsSubChapter saveSub = videoLessonsRepository.save(sub);

            });
        });
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

    public VideoLessonsReplyResponseDto createVideoLessonsReply(VideoLessonsReplyPostDto dto, User user) {
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        VideoLessonsReply newReply = VideoLessonsReply.create(dto, user);
        VideoLessonsReply saveReply = videoLessonsRepository.save(newReply);
        findVideo.setReplyCount(videoLessonsRepository.countReply(videoLessonsId));
        findVideo.setTotalStar(findVideo.getTotalStar()+saveReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar()/findVideo.getReplyCount());
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public VideoLessonsReplyResponseDto updateVideoLessonsReply(VideoLessonsReplyPatchDto dto, User user) {
        Long videoLessonsId = dto.getVideoLessonsId();
        VideoLessons findVideo = verifyVideoLessons(videoLessonsId);
        VideoLessonsReply findReply = verifyVideoLessonsReply(dto.getVideoLessonsReplyId());
        findReply.update(dto);
        VideoLessonsReply saveReply = videoLessonsRepository.save(findReply);
        findVideo.setTotalStar(findVideo.getTotalStar()-findReply.getStar());
        findVideo.setTotalStar(findVideo.getTotalStar()+saveReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar()/findVideo.getReplyCount());
        videoLessonsRepository.save(findVideo);
        return VideoLessonsReplyResponseDto.of(saveReply);
    }

    public void deleteVideoLessons(Long videoLessonsId) {
        videoLessonsRepository.deleteVideoLessons(verifyVideoLessons(videoLessonsId));
    }

    public void deleteVideoLessonsReply(Long videoLessonsReplyId) {
        VideoLessonsReply findReply = verifyVideoLessonsReply(videoLessonsReplyId);
        VideoLessons findVideo = verifyVideoLessons(findReply.getVideoLessonsId());
        findVideo.setTotalStar(findVideo.getTotalStar()-findReply.getStar());
        findVideo.setStarAverage(findVideo.getTotalStar()/findVideo.getReplyCount());
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
        Page<VideoLessonsUser> find = videoLessonsRepository.findAllVideoLessonsUser(userId, PageRequest.of(page, 5, Sort.by("createAt").descending()));
        return find.map(vu -> {
            VideoLessons videoLessons = verifyVideoLessons(vu.getVideoLessonsId());
            return VideoLessonsUserSimpleResponseDto.of(videoLessons, vu.getStatus());
        });
    }

    public VideoLessonsResponseDto createVideoLessonsUser(User user, Long videoLessonsId) {
        SubscribeUser findSubUser = subscribeRepository.findSubscribeUserByUserIdAndStatus(user.getUserId(), SubscribeStatus.ACTIVE)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (findSubUser.getVideoLessonsCount() > 0) {
            int videoLessonsCount = findSubUser.getVideoLessonsCount();
            findSubUser.setVideoLessonsCount(videoLessonsCount - 1);
            findSubUser.setVideoLessonsIdList(List.of(videoLessonsId));
            subscribeRepository.saveSubscribeUser(findSubUser);
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

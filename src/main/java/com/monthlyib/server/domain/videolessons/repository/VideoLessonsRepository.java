package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.api.videolessons.dto.VideoCategoryDetailResponseDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsSearchDto;
import com.monthlyib.server.domain.videolessons.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VideoLessonsRepository {

    Page<VideoLessons> findAll(Pageable pageable, VideoLessonsSearchDto dto);

    List<VideoLessons> findAllByIdList(List<Long> videoLessonIdList);

    Optional<VideoLessons> findVideoById(Long videoLessonsId);

    Optional<VideoLessonsCategory> findVideoCategory(Long videoLessonsCategoryId);

    Optional<VideoLessonsMainChapter> findVideoMainChapter(Long videoLessonsMainChapterId);

    List<VideoLessonsMainChapter> findVideoMainChapters(Long videoLessonsId);

    List<VideoLessonsSubChapter> findVideoSubChaptersByMainChapterId(Long videoLessonsMainChapterId);

    List<VideoLessonsSubChapter> findVideoSubChaptersByVideoLessonsId(Long videoLessonsId);

    Optional<VideoLessonsSubChapter> findVideoSubChapter(Long videoLessonsSubChapterId);

    Page<VideoLessonsReply> findVideoReply(Long videoLessonsId, Pageable pageable);

    Optional<VideoLessonsReply> findVideoReplyById(Long videoLessonsReplyId);

    List<VideoLessonsReply> findAllVideoLessonsReplies();

    List<VideoLessonsReply> findAllVideoLessonsRepliesByVideoLessonsId(Long videoLessonsId);

    boolean existsVideoLessonsReply(Long videoLessonsId, Long authorId);

    VideoLessons save(VideoLessons videoLessons);

    VideoLessonsCategory save(VideoLessonsCategory videoLessonsCategory);

    VideoLessonsMainChapter save(VideoLessonsMainChapter videoLessonsMainChapter);

    VideoLessonsSubChapter save(VideoLessonsSubChapter subChapter);

    VideoLessonsReply save(VideoLessonsReply reply);

    VideoThumbnail save(VideoThumbnail thumbnail);

    VideoLessonsUser save(VideoLessonsUser videoLessonsUser);

    VideoLessonsProgress save(VideoLessonsProgress videoLessonsProgress);

    Optional<VideoLessonsUser> findVideoLessonsUser(Long videoLessonsId, Long userId);

    Optional<VideoLessonsProgress> findVideoLessonsProgress(Long userId, Long videoLessonsId, Long subChapterId);

    Page<VideoLessonsUser> findAllVideoLessonsUser(Long userId, Pageable pageable);

    List<VideoLessonsUser> findAllVideoLessonsUser(Long userId);

    List<VideoLessonsProgress> findAllVideoLessonsProgress(Long userId, Long videoLessonsId);

    List<VideoThumbnail> findVideoThumbnailByVideoLessonsId(Long videoLessonsId);

    void deleteVideoLessons(VideoLessons videoLessons);
    void deleteVideoLessonsCategory(Long videoLessonsCategoryId);
    void deleteVideoLessonsMainChapter(Long videoLessonsMainChapterId);
    void deleteVideoLessonsSubChapter(Long videoLessonsSubChapterId);
    void deleteVideoLessonsReply(Long videoLessonsReplyId);
    void deleteVideoThumbnail(Long videoThumbnailId);

    void deleteAllVideoLessonsMainChapterByVideoLessonsId(Long videoLessonsId);
    void deleteAllVideoLessonsSubChapterByVideoLessonsId(Long videoLessonsId);
    void deleteAllVideoLessonsSubChapterByMainChapterId(Long mainChapterId);
    void deleteAllVideoLessonsReplyByVideoLessonsId(Long videoLessonsId);
    void deleteAllVideoThumbnailByVideoLessonsId(Long videoLessonsId);

    long countReply(Long videoLessonsId);

    VideoCategoryDetailResponseDto findAllCategory();




}

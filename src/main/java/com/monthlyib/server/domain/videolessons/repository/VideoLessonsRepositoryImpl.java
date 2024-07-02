package com.monthlyib.server.domain.videolessons.repository;

import com.monthlyib.server.api.videolessons.dto.VideoCategoryDetailResponseDto;
import com.monthlyib.server.api.videolessons.dto.VideoCategoryResponseDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsSearchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsSimpleResponseDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.constant.VideoLessonsStatus;
import com.monthlyib.server.domain.videolessons.entity.*;
import com.monthlyib.server.exception.ServiceLogicException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VideoLessonsRepositoryImpl extends QuerydslRepositorySupport implements VideoLessonsRepository {

    private final VideoLessonsJpaRepository videoLessonsJpaRepository;

    private final VideoLessonsReplyJpaRepository videoLessonsReplyJpaRepository;

    private final VideoLessonsSubChapterJpaRepository videoLessonsSubChapterJpaRepository;

    private final VideoLessonsCategoryJpaRepository videoLessonsCategoryJpaRepository;

    private final VideoLessonsMainChapterJpaRepository videoLessonsMainChapterJpaRepository;

    private final VideoLessonsThumbnailJpaRepository videoLessonsThumbnailJpaRepository;

    private final VideoLessonsUserJpaRepository videoLessonsUserJpaRepository;


    public VideoLessonsRepositoryImpl(
            VideoLessonsJpaRepository videoLessonsJpaRepository,
            VideoLessonsReplyJpaRepository videoLessonsReplyJpaRepository,
            VideoLessonsSubChapterJpaRepository videoLessonsSubChapterJpaRepository,
            VideoLessonsCategoryJpaRepository videoLessonsCategoryJpaRepository,
            VideoLessonsMainChapterJpaRepository videoLessonsMainChapterJpaRepository,
            VideoLessonsThumbnailJpaRepository videoLessonsThumbnailJpaRepository,
            VideoLessonsUserJpaRepository videoLessonsUserJpaRepository
    ) {
        super(VideoLessons.class);
        this.videoLessonsJpaRepository = videoLessonsJpaRepository;
        this.videoLessonsReplyJpaRepository = videoLessonsReplyJpaRepository;
        this.videoLessonsSubChapterJpaRepository = videoLessonsSubChapterJpaRepository;
        this.videoLessonsCategoryJpaRepository = videoLessonsCategoryJpaRepository;
        this.videoLessonsMainChapterJpaRepository = videoLessonsMainChapterJpaRepository;
        this.videoLessonsThumbnailJpaRepository = videoLessonsThumbnailJpaRepository;
        this.videoLessonsUserJpaRepository = videoLessonsUserJpaRepository;
    }

    QVideoLessons videoLessons = QVideoLessons.videoLessons;

    @Override
    public Page<VideoLessons> findAll(Pageable pageable, VideoLessonsSearchDto dto) {
        VideoLessonsStatus status = dto.getStatus();
        String keyWord = dto.getKeyWord();
        Long firstCategoryId = dto.getFirstCategoryId();
        Long secondCategoryId = dto.getSecondCategoryId();
        Long thirdCategoryId = dto.getThirdCategoryId();
        JPQLQuery<VideoLessons> query = from(videoLessons).select(videoLessons);

        if (status != null) {
            query.where(videoLessons.videoLessonsStatus.eq(status));
        }

        if (keyWord != null) {
            query.where(videoLessons.title.containsIgnoreCase(keyWord).or(videoLessons.title.containsIgnoreCase(keyWord)));
        }

        if (firstCategoryId != null) {
            query.where(videoLessons.firstCategoryId.eq(firstCategoryId));
        }

        if (secondCategoryId != null) {
            query.where(videoLessons.secondCategoryId.eq(secondCategoryId));
        }

        if (thirdCategoryId != null) {
            query.where(videoLessons.thirdCategoryId.eq(thirdCategoryId));
        }

        List<VideoLessons> list = Optional.ofNullable(getQuerydsl())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.DATA_ACCESS_ERROR))
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(list, pageable, query.fetchCount());
    }

    @Override
    public List<VideoLessons> findAllByIdList(List<Long> videoLessonIdList) {
        return videoLessonsJpaRepository.findAllById(videoLessonIdList);
    }

    @Override
    public Optional<VideoLessons> findVideoById(Long videoLessonsId) {
        return videoLessonsJpaRepository.findById(videoLessonsId);
    }

    @Override
    public Optional<VideoLessonsCategory> findVideoCategory(Long videoLessonsCategoryId) {
        return videoLessonsCategoryJpaRepository.findById(videoLessonsCategoryId);
    }

    @Override
    public Optional<VideoLessonsMainChapter> findVideoMainChapter(Long videoLessonsMainChapterId) {
        return videoLessonsMainChapterJpaRepository.findById(videoLessonsMainChapterId);
    }

    @Override
    public List<VideoLessonsMainChapter> findVideoMainChapters(Long videoLessonsId) {
        return videoLessonsMainChapterJpaRepository.findAllByVideoLessonsId(videoLessonsId);
    }



    @Override
    public List<VideoLessonsSubChapter> findVideoSubChaptersByMainChapterId(Long videoLessonsMainChapterId) {
        return videoLessonsSubChapterJpaRepository.findAllByMainChapterId(videoLessonsMainChapterId);
    }

    @Override
    public Optional<VideoLessonsSubChapter> findVideoSubChapter(Long videoLessonsSubChapterId) {
        return videoLessonsSubChapterJpaRepository.findById(videoLessonsSubChapterId);
    }

    @Override
    public Page<VideoLessonsReply> findVideoReply(Long videoLessonsId, Pageable pageable) {
        return videoLessonsReplyJpaRepository.findAllByVideoLessonsId(videoLessonsId, pageable);
    }

    @Override
    public Optional<VideoLessonsReply> findVideoReplyById(Long videoLessonsReplyId) {
        return videoLessonsReplyJpaRepository.findById(videoLessonsReplyId);
    }

    @Override
    public VideoLessons save(VideoLessons videoLessons) {
        return videoLessonsJpaRepository.save(videoLessons);
    }

    @Override
    public VideoLessonsCategory save(VideoLessonsCategory videoLessonsCategory) {
        return videoLessonsCategoryJpaRepository.save(videoLessonsCategory);
    }

    @Override
    public VideoLessonsMainChapter save(VideoLessonsMainChapter videoLessonsMainChapter) {
        return videoLessonsMainChapterJpaRepository.save(videoLessonsMainChapter);
    }

    @Override
    public VideoLessonsSubChapter save(VideoLessonsSubChapter subChapter) {
        return videoLessonsSubChapterJpaRepository.save(subChapter);
    }

    @Override
    public VideoLessonsReply save(VideoLessonsReply reply) {
        return videoLessonsReplyJpaRepository.save(reply);
    }

    @Override
    public VideoThumbnail save(VideoThumbnail thumbnail) {
        return videoLessonsThumbnailJpaRepository.save(thumbnail);
    }

    @Override
    public VideoLessonsUser save(VideoLessonsUser videoLessonsUser) {
        return videoLessonsUserJpaRepository.save(videoLessonsUser);
    }

    @Override
    public Optional<VideoLessonsUser> findVideoLessonsUser(Long videoLessonsId, Long userId) {
        return videoLessonsUserJpaRepository.findByUserIdAndVideoLessonsId(userId, videoLessonsId);
    }

    @Override
    public Page<VideoLessonsUser> findAllVideoLessonsUser(Long userId, Pageable pageable) {
        return videoLessonsUserJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public List<VideoThumbnail> findVideoThumbnailByVideoLessonsId(Long videoLessonsId) {
        return videoLessonsThumbnailJpaRepository.findAllByVideoLessonsId(videoLessonsId);
    }

    @Override
    public void deleteVideoLessons(VideoLessons videoLessons) {
        videoLessonsMainChapterJpaRepository.deleteAllByVideoLessonsId(videoLessons.getVideoLessonsId());
        videoLessonsSubChapterJpaRepository.deleteAllByVideoLessonsId(videoLessons.getVideoLessonsId());
        videoLessonsReplyJpaRepository.deleteAllByVideoLessonsId(videoLessons.getVideoLessonsId());
        videoLessonsThumbnailJpaRepository.deleteAllByVideoLessonsId(videoLessons.getVideoLessonsId());
        videoLessonsJpaRepository.delete(videoLessons);
    }

    @Override
    public void deleteVideoLessonsCategory(Long videoLessonsCategoryId) {
        videoLessonsCategoryJpaRepository.deleteById(videoLessonsCategoryId);
    }

    @Override
    public void deleteVideoLessonsMainChapter(Long videoLessonsMainChapterId) {
        videoLessonsMainChapterJpaRepository.deleteById(videoLessonsMainChapterId);
    }

    @Override
    public void deleteVideoLessonsSubChapter(Long videoLessonsSubChapterId) {
        videoLessonsSubChapterJpaRepository.deleteById(videoLessonsSubChapterId);
    }

    @Override
    public void deleteVideoLessonsReply(Long videoLessonsReplyId) {
        videoLessonsReplyJpaRepository.deleteById(videoLessonsReplyId);
    }

    @Override
    public void deleteVideoThumbnail(Long videoThumbnailId) {
        videoLessonsThumbnailJpaRepository.deleteById(videoThumbnailId);
    }

    @Override
    public void deleteAllVideoLessonsMainChapterByVideoLessonsId(Long videoLessonsId) {
        videoLessonsMainChapterJpaRepository.deleteAllByVideoLessonsId(videoLessonsId);

    }

    @Override
    public void deleteAllVideoLessonsSubChapterByVideoLessonsId(Long videoLessonsId) {
        videoLessonsSubChapterJpaRepository.deleteAllByVideoLessonsId(videoLessonsId);

    }

    @Override
    public void deleteAllVideoLessonsReplyByVideoLessonsId(Long videoLessonsId) {
        videoLessonsReplyJpaRepository.deleteAllByVideoLessonsId(videoLessonsId);
    }

    @Override
    public void deleteAllVideoThumbnailByVideoLessonsId(Long videoLessonsId) {
        videoLessonsThumbnailJpaRepository.deleteAllByVideoLessonsId(videoLessonsId);
    }

    @Override
    public long countReply(Long videoLessonsId) {
        return videoLessonsReplyJpaRepository.countAllByVideoLessonsId(videoLessonsId);
    }

    @Override
    public VideoCategoryDetailResponseDto findAllCategory() {
        List<VideoLessonsCategory> first = videoLessonsCategoryJpaRepository.findAllByVideoCategoryStatus(VideoCategoryStatus.FIRST_CATEGORY);
        List<VideoLessonsCategory> second = videoLessonsCategoryJpaRepository.findAllByVideoCategoryStatus(VideoCategoryStatus.SECOND_CATEGORY);
        List<VideoLessonsCategory> third = videoLessonsCategoryJpaRepository.findAllByVideoCategoryStatus(VideoCategoryStatus.THIRD_CATEGORY);
        return VideoCategoryDetailResponseDto.of(
                first.stream().map(f -> VideoCategoryResponseDto.of(f)).toList(),
                second.stream().map(f -> VideoCategoryResponseDto.of(f)).toList(),
                third.stream().map(f -> VideoCategoryResponseDto.of(f)).toList()
        );
    }


    private JPQLQuery<VideoLessonsSimpleResponseDto> getVideoSimpleQuery() {
        return from(videoLessons)
                .select(
                        Projections.constructor(
                                VideoLessonsSimpleResponseDto.class,
                                videoLessons.videoLessonsId,
                                videoLessons.title,
                                videoLessons.content,
                                videoLessons.videoLessonsThumbnailId,
                                videoLessons.videoLessonsIbThumbnailUrl,
                                videoLessons.createAt,
                                videoLessons.updateAt
                        )
                );
    }
}

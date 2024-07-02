package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessons;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsSimpleResponseDto {

    private Long videoLessonsId;

    private String title;

    private String content;

    private VideoCategoryResponseDto firstCategory;

    private VideoCategoryResponseDto secondCategory;

    private VideoCategoryResponseDto thirdCategory;

    private Long videoLessonsThumbnailId;

    private String videoLessonsIbThumbnailUrl;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;


    public VideoLessonsSimpleResponseDto(
            Long videoLessonsId,
            String title,
            String content,
            Long videoLessonsThumbnailId,
            String videoLessonsIbThumbnailUrl,
            LocalDateTime createAt,
            LocalDateTime updateAt) {
        this.videoLessonsId = videoLessonsId;
        this.title = title;
        this.content = content;
        this.videoLessonsThumbnailId = videoLessonsThumbnailId;
        this.videoLessonsIbThumbnailUrl = videoLessonsIbThumbnailUrl;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }


    public static VideoLessonsSimpleResponseDto of(VideoLessons videoLessons) {
        return VideoLessonsSimpleResponseDto.builder()
                .videoLessonsId(videoLessons.getVideoLessonsId())
                .title(videoLessons.getTitle())
                .content(videoLessons.getContent())
                .firstCategory(VideoCategoryResponseDto.of(videoLessons.getFirstCategoryId(), VideoCategoryStatus.FIRST_CATEGORY, videoLessons.getFirstCategoryName()))
                .secondCategory(VideoCategoryResponseDto.of(videoLessons.getSecondCategoryId(), VideoCategoryStatus.SECOND_CATEGORY, videoLessons.getSecondCategoryName()))
                .thirdCategory(VideoCategoryResponseDto.of(videoLessons.getThirdCategoryId(), VideoCategoryStatus.THIRD_CATEGORY, videoLessons.getThirdCategoryName()))

                .videoLessonsThumbnailId(videoLessons.getVideoLessonsThumbnailId())
                .videoLessonsIbThumbnailUrl(videoLessons.getVideoLessonsIbThumbnailUrl())
                .createAt(videoLessons.getCreateAt())
                .updateAt(videoLessons.getUpdateAt())
                .build();
    }


}

package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.constant.VideoLessonsStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessons;
import com.monthlyib.server.dto.PageResponseDto;
import com.monthlyib.server.dto.Result;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsResponseDto {

    private Long videoLessonsId;

    private String title;

    private String content;

    //강사
    private String instructor;

    //쳅터 정보(몇챕터까지 있는지)
    private String chapterInfo;

    // 수강 시 기간
    private String duration;

    private Long videoLessonsThumbnailId;

    private String videoLessonsIbThumbnailUrl;

    private double starAverage;

    private long replyCount;

    private List<VideoLessonsChapterResponseDto> chapters;

    private PageResponseDto<List<VideoLessonsReplyResponseDto>> reply;

    private VideoCategoryResponseDto firstCategory;

    private VideoCategoryResponseDto secondCategory;

    private VideoCategoryResponseDto thirdCategory;

    private VideoLessonsStatus videoLessonsStatus;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static VideoLessonsResponseDto of(
            VideoLessons videoLessons,
            List<VideoLessonsChapterResponseDto> chapters,
            Page<VideoLessonsReplyResponseDto> reply
    ) {
        Page<VideoLessonsReplyResponseDto> replyPage = Optional.ofNullable(reply).orElse(Page.empty());
        return VideoLessonsResponseDto.builder()
                .videoLessonsId(videoLessons.getVideoLessonsId())
                .title(videoLessons.getTitle())
                .content(videoLessons.getContent())
                .instructor(videoLessons.getInstructor())
                .chapterInfo(videoLessons.getChapterInfo())
                .duration(videoLessons.getDuration())
                .videoLessonsThumbnailId(videoLessons.getVideoLessonsThumbnailId())
                .videoLessonsIbThumbnailUrl(videoLessons.getVideoLessonsIbThumbnailUrl())
                .starAverage(videoLessons.getStarAverage())
                .replyCount(videoLessons.getReplyCount())
                .chapters(chapters)
                .reply(PageResponseDto.of(
                        replyPage,
                        replyPage.getContent(),
                        Result.ok()))
                .firstCategory(VideoCategoryResponseDto.of(videoLessons.getFirstCategoryId(), VideoCategoryStatus.FIRST_CATEGORY, videoLessons.getFirstCategoryName()))
                .secondCategory(VideoCategoryResponseDto.of(videoLessons.getSecondCategoryId(), VideoCategoryStatus.SECOND_CATEGORY, videoLessons.getSecondCategoryName()))
                .thirdCategory(VideoCategoryResponseDto.of(videoLessons.getThirdCategoryId(), VideoCategoryStatus.THIRD_CATEGORY, videoLessons.getThirdCategoryName()))
                .createAt(videoLessons.getCreateAt())
                .updateAt(videoLessons.getUpdateAt())
                .build();
    }

}

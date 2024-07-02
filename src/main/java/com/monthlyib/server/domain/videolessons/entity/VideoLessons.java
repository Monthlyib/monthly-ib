package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.api.videolessons.dto.VideoCategoryResponseDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.constant.VideoLessonsStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "video_lessons")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessons extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private String chapterInfo;

    @Column(nullable = false)
    private String duration;

    // 썸네일 객체
    @Column(nullable = false)
    private Long videoLessonsThumbnailId;

    @Column(nullable = false)
    private String videoLessonsIbThumbnailUrl;

    @Column(nullable = false)
    private double starAverage;

    @Column(nullable = false)
    private double totalStar;

    @Column(nullable = false)
    private long replyCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoLessonsStatus videoLessonsStatus;

    @Column(nullable = false)
    private Long firstCategoryId;

    @Column(nullable = false)
    private String firstCategoryName;

    @Column(nullable = false)
    private Long secondCategoryId;

    @Column(nullable = false)
    private String secondCategoryName;

    @Column(nullable = false)
    private Long thirdCategoryId;

    @Column(nullable = false)
    private String thirdCategoryName;

    public static VideoLessons create(
            VideoLessonsPostDto dto,
            VideoLessonsCategory firstCategory,
            VideoLessonsCategory secondCategory,
            VideoLessonsCategory thirdCategory
    ) {

        return VideoLessons.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .instructor(dto.getInstructor())
                .chapterInfo(dto.getChapterInfo())
                .duration(dto.getDuration())
                .firstCategoryId(firstCategory.getVideoCategoryId())
                .firstCategoryName(firstCategory.getCategoryName())
                .secondCategoryId(secondCategory.getVideoCategoryId())
                .secondCategoryName(secondCategory.getCategoryName())
                .thirdCategoryId(thirdCategory.getVideoCategoryId())
                .thirdCategoryName(thirdCategory.getCategoryName())
                .videoLessonsStatus(VideoLessonsStatus.INACTIVE)
                .starAverage(0)
                .replyCount(0L)
                .totalStar(0)
                .videoLessonsThumbnailId(0L)
                .videoLessonsIbThumbnailUrl("")
                .build();
    }

    public VideoLessons update(
            VideoLessonsPatchDto dto,
            VideoLessonsCategory firstCategory,
            VideoLessonsCategory secondCategory,
            VideoLessonsCategory thirdCategory
    ) {
        this.title = Optional.ofNullable(dto.getTitle()).orElse(this.getTitle());
        this.content = Optional.ofNullable(dto.getContent()).orElse(this.getContent());
        this.instructor = Optional.ofNullable(dto.getInstructor()).orElse(this.getInstructor());
        this.chapterInfo = Optional.ofNullable(dto.getChapterInfo()).orElse(this.getChapterInfo());
        this.duration = Optional.ofNullable(dto.getDuration()).orElse(this.getDuration());
        this.firstCategoryId = firstCategory.getVideoCategoryId();
        this.firstCategoryName = firstCategory.getCategoryName();
        this.secondCategoryId = secondCategory.getVideoCategoryId();
        this.secondCategoryName = secondCategory.getCategoryName();
        this.thirdCategoryId = thirdCategory.getVideoCategoryId();
        this.thirdCategoryName = thirdCategory.getCategoryName();
        this.videoLessonsStatus = Optional.ofNullable(dto.getVideoLessonsStatus()).orElse(this.getVideoLessonsStatus());
        return this;
    }

}

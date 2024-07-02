package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.api.videolessons.dto.VideoLessonsChapterPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsChapterPostDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsSubChapterPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsSubChapterPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoChapterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_sub_chapter")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsSubChapter extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsSubChapterId;

    @Column(nullable = false)
    private Long mainChapterId;

    @Column(nullable = false)
    private Long videoLessonsId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoChapterStatus chapterStatus;

    @Column(nullable = false)
    private String chapterTitle;

    @Column(nullable = false)
    private int chapterIndex;

    @Column(nullable = false)
    private String videoFileUrl;

    public static VideoLessonsSubChapter create(Long videoLessonsId, Long mainChapterId, VideoLessonsSubChapterPostDto dto) {
        return VideoLessonsSubChapter.builder()
                .mainChapterId(mainChapterId)
                .videoLessonsId(videoLessonsId)
                .chapterStatus(VideoChapterStatus.SUB_CHAPTER)
                .chapterTitle(dto.getChapterTitle())
                .chapterIndex(dto.getChapterIndex())
                .videoFileUrl(dto.getVideoFileUrl())
                .build();
    }

    public VideoLessonsSubChapter update(VideoLessonsSubChapterPatchDto dto) {
        this.chapterTitle = Optional.ofNullable(dto.getChapterTitle()).orElse(this.getChapterTitle());
        this.chapterIndex = Optional.ofNullable(dto.getChapterIndex()).orElse(this.getChapterIndex());
        this.chapterStatus = Optional.ofNullable(dto.getChapterStatus()).orElse(this.getChapterStatus());
        this.videoFileUrl = Optional.ofNullable(dto.getVideoFileUrl()).orElse(this.getVideoFileUrl());
        return this;
    }
}

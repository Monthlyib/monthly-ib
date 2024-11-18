package com.monthlyib.server.domain.videolessons.entity;


import com.monthlyib.server.api.videolessons.dto.VideoLessonsChapterPatchDto;
import com.monthlyib.server.api.videolessons.dto.VideoLessonsChapterPostDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.VideoChapterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Setter
@Getter
@Entity
@Table(name = "video_lessons_main_chapter")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonsMainChapter extends Auditable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoLessonsMainChapterId;

    @Column(nullable = false)
    private Long videoLessonsId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoChapterStatus chapterStatus;

    @Column(nullable = false)
    private String chapterTitle;

    @Column(nullable = false)
    private int chapterIndex;

    public static VideoLessonsMainChapter create(Long videoLessonsId,VideoLessonsChapterPostDto dto) {
        return VideoLessonsMainChapter.builder()
                .videoLessonsId(videoLessonsId)
                .chapterStatus(VideoChapterStatus.MAIN_CHAPTER)
                .chapterTitle(dto.getChapterTitle())
                .chapterIndex(dto.getChapterIndex())
                .build();
    }

    public VideoLessonsMainChapter update(VideoLessonsChapterPatchDto dto) {
        this.chapterTitle = Optional.ofNullable(dto.getChapterTitle()).orElse(this.getChapterTitle());
        this.chapterIndex = Optional.ofNullable(dto.getChapterIndex()).orElse(this.getChapterIndex());
        this.chapterStatus = Optional.ofNullable(dto.getChapterStatus()).orElse(this.getChapterStatus());
        return this;
    }


}

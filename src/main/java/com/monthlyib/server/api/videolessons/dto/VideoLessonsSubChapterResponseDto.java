package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoChapterStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsSubChapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsSubChapterResponseDto implements Comparable<VideoLessonsSubChapterResponseDto> {

    private Long chapterId;

    private Long mainChapterId;

    private VideoChapterStatus chapterStatus;

    private String chapterTitle;

    private Integer chapterIndex;

    private String videoFileUrl;

    public static VideoLessonsSubChapterResponseDto of(VideoLessonsSubChapter subChapter) {
        return VideoLessonsSubChapterResponseDto.builder()
                .chapterId(subChapter.getVideoLessonsSubChapterId())
                .mainChapterId(subChapter.getMainChapterId())
                .chapterStatus(subChapter.getChapterStatus())
                .chapterTitle(subChapter.getChapterTitle())
                .chapterIndex(subChapter.getChapterIndex())
                .videoFileUrl(subChapter.getVideoFileUrl())
                .build();
    }

    @Override
    public int compareTo(VideoLessonsSubChapterResponseDto o) {
        return this.chapterIndex.compareTo(o.chapterIndex);
    }
}

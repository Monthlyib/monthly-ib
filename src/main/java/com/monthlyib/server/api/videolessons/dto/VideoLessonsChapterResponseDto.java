package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoChapterStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsMainChapter;
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
public class VideoLessonsChapterResponseDto implements Comparable<VideoLessonsChapterResponseDto> {

    private Long chapterId;

    private VideoChapterStatus chapterStatus;

    private String chapterTitle;

    private Integer chapterIndex;

    private List<VideoLessonsSubChapterResponseDto> subChapters;

    public static VideoLessonsChapterResponseDto of(VideoLessonsMainChapter mainChapter, List<VideoLessonsSubChapterResponseDto> subChapters) {
        return VideoLessonsChapterResponseDto.builder()
                .chapterId(mainChapter.getVideoLessonsMainChapterId())
                .chapterStatus(mainChapter.getChapterStatus())
                .chapterTitle(mainChapter.getChapterTitle())
                .chapterIndex(mainChapter.getChapterIndex())
                .subChapters(subChapters)
                .build();
    }



    @Override
    public int compareTo(VideoLessonsChapterResponseDto o) {
        return this.chapterIndex.compareTo(o.chapterIndex);
    }
}

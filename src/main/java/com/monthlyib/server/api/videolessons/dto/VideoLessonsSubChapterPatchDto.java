package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsSubChapterPatchDto {

    private Long chapterId;

    private VideoChapterStatus chapterStatus;

    private String chapterTitle;

    private int chapterIndex;

    private String videoFileUrl;

}

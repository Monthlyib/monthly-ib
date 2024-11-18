package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsSubChapterPostDto {

    private VideoChapterStatus chapterStatus;

    private String chapterTitle;

    private int chapterIndex;

    private String videoFileUrl;

}

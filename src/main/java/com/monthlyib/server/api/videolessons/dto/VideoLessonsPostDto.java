package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsPostDto {

    private String title;

    private String content;

    //강사
    private String instructor;

    //쳅터 정보(몇챕터까지 있는지)
    private String chapterInfo;

    // 수강 시 기간
    private String duration;

    private List<VideoLessonsChapterPostDto> chapters;

    private Long firstCategoryId;

    private Long secondCategoryId;

    private Long thirdCategoryId;


}

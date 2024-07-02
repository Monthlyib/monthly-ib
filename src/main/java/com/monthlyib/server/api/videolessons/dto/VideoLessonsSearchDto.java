package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoChapterStatus;
import com.monthlyib.server.constant.VideoLessonsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoLessonsSearchDto {

    private int page;

    private String keyWord;

    private Long firstCategoryId;

    private Long secondCategoryId;

    private Long thirdCategoryId;

    private VideoLessonsStatus status;

}

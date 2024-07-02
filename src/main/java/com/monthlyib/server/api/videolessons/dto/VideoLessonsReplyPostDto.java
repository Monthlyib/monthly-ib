package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsReplyPostDto {

    private Long videoLessonsId;

    private Long authorId;

    private String content;

    private double star;

}

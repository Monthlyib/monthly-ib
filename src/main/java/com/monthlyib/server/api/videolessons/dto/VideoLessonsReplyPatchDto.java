package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsReplyPatchDto {

    private Long videoLessonsReplyId;

    private Long videoLessonsId;

    private String content;

    private double star;

}

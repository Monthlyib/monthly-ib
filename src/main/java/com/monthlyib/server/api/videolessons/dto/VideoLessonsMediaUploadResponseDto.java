package com.monthlyib.server.api.videolessons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoLessonsMediaUploadResponseDto {

    private String fileUrl;

    private String fileName;

    private String contentType;
}

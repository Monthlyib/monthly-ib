package com.monthlyib.server.api.videolessons.dto;


import com.monthlyib.server.constant.VideoCategoryStatus;
import com.monthlyib.server.domain.videolessons.entity.VideoLessonsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCategoryResponseDto {

    private Long videoCategoryId;

    private VideoCategoryStatus videoCategoryStatus;

    private String categoryName;

    private Long parentsCategoryId;

    public static VideoCategoryResponseDto of(Long videoCategoryId, VideoCategoryStatus videoCategoryStatus, String categoryName) {
        return VideoCategoryResponseDto.builder()
                .videoCategoryId(videoCategoryId)
                .videoCategoryStatus(videoCategoryStatus)
                .categoryName(categoryName)
                .build();

    }

    public static VideoCategoryResponseDto of(VideoLessonsCategory category) {
        return VideoCategoryResponseDto.builder()
                .videoCategoryId(category.getVideoCategoryId())
                .videoCategoryStatus(category.getVideoCategoryStatus())
                .categoryName(category.getCategoryName())
                .parentsCategoryId(category.getParentsCategoryId())
                .build();

    }
}

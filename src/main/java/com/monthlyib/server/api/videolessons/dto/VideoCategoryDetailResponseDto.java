package com.monthlyib.server.api.videolessons.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCategoryDetailResponseDto {

    private List<VideoCategoryResponseDto> firstCategory;
    private List<VideoCategoryResponseDto> secondCategory;
    private List<VideoCategoryResponseDto> thirdCategory;

    public static VideoCategoryDetailResponseDto of(List<VideoCategoryResponseDto> firstCategory, List<VideoCategoryResponseDto> secondCategory, List<VideoCategoryResponseDto> thirdCategory) {
        return VideoCategoryDetailResponseDto.builder()
                .firstCategory(firstCategory)
                .secondCategory(secondCategory)
                .thirdCategory(thirdCategory)
                .build();
    }

}

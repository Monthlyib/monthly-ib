package com.monthlyib.server.api.videolessons.dto;

import com.monthlyib.server.constant.VideoCategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCategorySearchDto {

    private VideoCategoryStatus videoCategoryStatus;

}

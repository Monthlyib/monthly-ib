package com.monthlyib.server.api.headernavigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderNavigationPublicResponseDto {

    private String pageKey;

    private HeaderNavigationConfigDto config;

    private LocalDateTime updatedAt;
}

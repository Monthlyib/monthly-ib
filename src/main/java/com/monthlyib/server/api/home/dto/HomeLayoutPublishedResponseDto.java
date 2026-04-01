package com.monthlyib.server.api.home.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutPublishedResponseDto {

    private String pageKey;

    private HomeLayoutContentDto layout;

    private LocalDateTime publishedAt;
}

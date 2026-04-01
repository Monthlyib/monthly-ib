package com.monthlyib.server.api.home.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutAdminResponseDto {

    private String pageKey;

    private HomeLayoutContentDto draft;

    private HomeLayoutContentDto published;

    private Long draftUpdatedBy;

    private Long publishedBy;

    private LocalDateTime draftUpdatedAt;

    private LocalDateTime publishedAt;
}

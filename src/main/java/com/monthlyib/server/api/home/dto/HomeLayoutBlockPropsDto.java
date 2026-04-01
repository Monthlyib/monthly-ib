package com.monthlyib.server.api.home.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutBlockPropsDto {

    private String eyebrow;

    private String title;

    private String description;

    private String html;

    private String fileUrl;

    private String alt;

    private String caption;

    private String linkUrl;

    private String sourceType;

    private String embedUrl;

    private Integer height;

    private String label;

    private String href;

    private String variant;
}

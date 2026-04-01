package com.monthlyib.server.api.home.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLayoutMediaResponseDto {

    private String fileUrl;

    private String mediaType;

    private String fileName;
}

package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiioPostDto {
    private String iocTopic;
    private String workTitle;
    private String author;
}
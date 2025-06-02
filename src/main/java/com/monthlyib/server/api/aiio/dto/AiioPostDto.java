package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiioPostDto {
    private String iocTopic;
    private String workTitle;
    private String author;
    
    // 추가: 대본 파일과 녹음 파일
    private MultipartFile scriptFile;
    private MultipartFile audioFile;
}
package com.monthlyib.server.api.aiIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * English 전용 채팅 요청 DTO
 * subject: "Langauge A English" (오타 포함 기존 값 유지)
 * textType: "Language" | "Literature"
 * responseMode: "generative" | "evaluate"
 * prompt: 사용자가 입력한 내용
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnglishChatRequestDto {
    private String subject;      // 항상 "Langauge A English"
    private String textType;     // Language | Literature
    private String responseMode; // generative | evaluate
    private String prompt;       // 사용자 프롬프트
}
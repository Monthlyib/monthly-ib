package com.monthlyib.server.api.aiia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiIAEnglishChatRequestDto {

    private String subject;
    private String prompt;
    private String textType;
    private String responseMode;
}

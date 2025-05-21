package com.monthlyib.server.api.aidescriptive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiDescriptiveTestDto {
    private String question;
    private String subject;
    private String chapter;
}

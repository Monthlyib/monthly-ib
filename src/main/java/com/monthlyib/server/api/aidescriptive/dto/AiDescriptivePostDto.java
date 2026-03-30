package com.monthlyib.server.api.aidescriptive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptivePostDto {

    private String subject;
    private String chapter;
    private String question;
    private Integer maxScore = 20;
}

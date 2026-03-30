package com.monthlyib.server.api.aiio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizStartDto {

    private String subject;
    private String chapter;
    private Integer durationMinutes = 30;
}

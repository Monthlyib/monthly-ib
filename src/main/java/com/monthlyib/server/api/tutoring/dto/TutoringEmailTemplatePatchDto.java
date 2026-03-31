package com.monthlyib.server.api.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringEmailTemplatePatchDto {
    private String subject;
    private String bodyTemplate;
    private Boolean active;
}

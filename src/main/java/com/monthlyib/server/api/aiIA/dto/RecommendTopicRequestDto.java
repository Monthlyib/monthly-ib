package com.monthlyib.server.api.aiIA.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecommendTopicRequestDto {
    private String subject;
    private String interest;
}

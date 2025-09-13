package com.monthlyib.server.api.aiIA.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicGuideRequestDto {
    private String subject;
    private String interestTopic;
    private Map<String, Object> topic;
}

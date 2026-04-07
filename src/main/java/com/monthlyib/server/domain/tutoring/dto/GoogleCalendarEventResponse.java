package com.monthlyib.server.domain.tutoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCalendarEventResponse {

    private String id;

    @JsonProperty("htmlLink")
    private String htmlLink;
}

package com.monthlyib.server.api.finance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class FinanceSyncJobResponseDto {

    private Long syncJobId;
    private String jobStatus;
    private String triggerType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime startedAt;
    private String message;
}

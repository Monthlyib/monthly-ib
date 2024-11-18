package com.monthlyib.server.api.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TutoringTimeSearchDto {

    private LocalDate date;

    private int hour;

    private int minute;

}

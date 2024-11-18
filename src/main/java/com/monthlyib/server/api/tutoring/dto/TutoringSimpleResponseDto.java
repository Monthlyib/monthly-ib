package com.monthlyib.server.api.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TutoringSimpleResponseDto {

    private LocalDate date;

    private List<TutoringRemainDto> currentTutoring;

    public static TutoringSimpleResponseDto of(LocalDate date, List<TutoringRemainDto> currentTutoring) {
        return TutoringSimpleResponseDto.builder()
                .date(date)
                .currentTutoring(currentTutoring)
                .build();
    }

}

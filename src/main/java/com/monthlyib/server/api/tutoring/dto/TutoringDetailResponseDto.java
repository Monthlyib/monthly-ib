package com.monthlyib.server.api.tutoring.dto;

import com.monthlyib.server.dto.PageResponseDto;
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
public class TutoringDetailResponseDto {

    private LocalDate date;

    private PageResponseDto<List<TutoringResponseDto>> tutoring;

    public static TutoringDetailResponseDto of(LocalDate date, PageResponseDto<List<TutoringResponseDto>> tutoring) {
        return TutoringDetailResponseDto.builder()
                .date(date)
                .tutoring(tutoring)
                .build();
    }

}

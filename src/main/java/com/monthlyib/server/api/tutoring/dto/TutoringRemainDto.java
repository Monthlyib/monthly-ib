package com.monthlyib.server.api.tutoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutoringRemainDto {

    private int hour;

    private int minute;

    private int remainTutoring;

    private int totalTutoring;

    private List<Long> tutoringList;

    public static TutoringRemainDto of(int hour, int minute, int remainTutoring, int totalTutoring, List<Long> tutoringList) {
        return TutoringRemainDto.builder()
                .hour(hour)
                .minute(minute)
                .remainTutoring(remainTutoring)
                .totalTutoring(totalTutoring)
                .tutoringList(tutoringList)
                .build();
    }

}

package com.monthlyib.server.api.tutoring.dto;


import com.monthlyib.server.constant.TutoringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TutoringPatchRequestDto {

    private String detail;

    private TutoringStatus tutoringStatus;

}

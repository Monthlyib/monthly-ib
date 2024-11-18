package com.monthlyib.server.api.tutoring.dto;

import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutoringResponseDto {

    private Long tutoringId;

    private LocalDate date;

    private int hour;

    private int minute;

    private Long requestUserId;

    private String requestUsername;

    private String requestUserNickName;

    private String detail;

    private TutoringStatus tutoringStatus;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public static TutoringResponseDto of(Tutoring tutoring) {
        return TutoringResponseDto.builder()
                .tutoringId(tutoring.getTutoringId())
                .date(tutoring.getDate())
                .hour(tutoring.getHour())
                .minute(tutoring.getMinute())
                .requestUserId(tutoring.getRequestUserId())
                .requestUsername(tutoring.getRequestUsername())
                .requestUserNickName(tutoring.getRequestUserNickName())
                .detail(tutoring.getDetail())
                .tutoringStatus(tutoring.getTutoringStatus())
                .createAt(tutoring.getCreateAt())
                .updateAt(tutoring.getUpdateAt())
                .build();
    }

}

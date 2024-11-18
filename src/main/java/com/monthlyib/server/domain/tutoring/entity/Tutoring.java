package com.monthlyib.server.domain.tutoring.entity;

import com.monthlyib.server.api.tutoring.dto.TutoringPostRequestDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.TutoringStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "tutorings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tutoring extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tutoringId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private int minute;

    @Column(nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private String requestUsername;

    @Column(nullable = false)
    private String requestUserNickName;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String detail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TutoringStatus tutoringStatus;

    public static Tutoring create(TutoringPostRequestDto dto, String requestUsername, String requestUserNickName) {
        return Tutoring.builder()
                .date(dto.getDate())
                .hour(dto.getHour())
                .minute(dto.getMinute())
                .requestUserId(dto.getRequestUserId())
                .requestUsername(requestUsername)
                .requestUserNickName(requestUserNickName)
                .detail(dto.getDetail())
                .tutoringStatus(TutoringStatus.WAIT)
                .build();
    }

}

package com.monthlyib.server.domain.tutoring.entity;

import com.monthlyib.server.api.tutoring.dto.TutoringPostRequestDto;
import com.monthlyib.server.audit.Auditable;
import com.monthlyib.server.constant.GoogleCalendarSyncStatus;
import com.monthlyib.server.constant.TutoringStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "tutorings",
        indexes = {
                @Index(name = "idx_tutorings_date_time", columnList = "date,hour,minute"),
                @Index(name = "idx_tutorings_user_status", columnList = "request_user_id,tutoring_status"),
                @Index(name = "idx_tutorings_created", columnList = "create_at")
        }
)
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

    private Long subscribeUserId;

    private String googleCalendarEventId;

    private String googleCalendarHtmlLink;

    @Enumerated(EnumType.STRING)
    private GoogleCalendarSyncStatus googleCalendarSyncStatus;

    @Column(columnDefinition = "LONGTEXT")
    private String googleCalendarLastError;

    private LocalDateTime googleCalendarSyncedAt;

    public static Tutoring create(TutoringPostRequestDto dto, String requestUsername, String requestUserNickName, Long subscribeUserId) {
        return Tutoring.builder()
                .date(dto.getDate())
                .hour(dto.getHour())
                .minute(dto.getMinute())
                .requestUserId(dto.getRequestUserId())
                .requestUsername(requestUsername)
                .requestUserNickName(requestUserNickName)
                .detail(dto.getDetail())
                .tutoringStatus(TutoringStatus.WAIT)
                .subscribeUserId(subscribeUserId)
                .build();
    }

    public void markGoogleCalendarPending() {
        this.googleCalendarSyncStatus = GoogleCalendarSyncStatus.PENDING;
        this.googleCalendarLastError = null;
    }

    public void markGoogleCalendarFailed(String errorMessage) {
        this.googleCalendarSyncStatus = GoogleCalendarSyncStatus.FAILED;
        this.googleCalendarLastError = normalizeCalendarError(errorMessage);
    }

    public void markGoogleCalendarSynced(String eventId, String htmlLink) {
        this.googleCalendarSyncStatus = GoogleCalendarSyncStatus.SYNCED;
        this.googleCalendarEventId = eventId;
        this.googleCalendarHtmlLink = htmlLink;
        this.googleCalendarLastError = null;
        this.googleCalendarSyncedAt = LocalDateTime.now();
    }

    public void markGoogleCalendarDeleted() {
        this.googleCalendarSyncStatus = GoogleCalendarSyncStatus.SYNCED;
        this.googleCalendarEventId = null;
        this.googleCalendarHtmlLink = null;
        this.googleCalendarLastError = null;
        this.googleCalendarSyncedAt = LocalDateTime.now();
    }

    private String normalizeCalendarError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Google Calendar 동기화에 실패했습니다.";
        }

        String trimmed = errorMessage.trim();
        if (trimmed.length() <= 500) {
            return trimmed;
        }

        return trimmed.substring(0, 500);
    }

}

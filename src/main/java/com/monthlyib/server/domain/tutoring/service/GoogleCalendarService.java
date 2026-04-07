package com.monthlyib.server.domain.tutoring.service;

import com.monthlyib.server.auth.dto.GoogleTokenResponse;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.tutoring.dto.GoogleCalendarEventResponse;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private static final String SEOUL_TIMEZONE = "Asia/Seoul";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Value("${oauth.google.client-id:}")
    private String clientId;

    @Value("${oauth.google.client-secret:}")
    private String clientSecret;

    @Value("${oauth.google.url.auth}")
    private String authUrl;

    @Value("${oauth.google.url.calendar-api}")
    private String calendarApiUrl;

    @Value("${oauth.google.calendar.refresh-token:}")
    private String refreshToken;

    @Value("${oauth.google.calendar.calendar-id:primary}")
    private String calendarId;

    private final RestTemplate restTemplate;

    public boolean isConfigured() {
        return hasText(clientId) && hasText(clientSecret) && hasText(refreshToken);
    }

    public String getConfigurationErrorMessage() {
        return "Google Calendar 연동 설정이 누락되었습니다.";
    }

    public GoogleCalendarEventResponse upsertTutoringEvent(Tutoring tutoring) {
        validateConfigured();

        String accessToken = refreshAccessToken();
        String encodedCalendarId = encodePath(calendarId);
        String baseUrl = String.format("%s/calendars/%s/events", calendarApiUrl, encodedCalendarId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(buildEventPayload(tutoring), headers);

        try {
            ResponseEntity<GoogleCalendarEventResponse> response;
            if (hasText(tutoring.getGoogleCalendarEventId())) {
                response = restTemplate.exchange(
                        baseUrl + "/" + encodePath(tutoring.getGoogleCalendarEventId()),
                        HttpMethod.PUT,
                        request,
                        GoogleCalendarEventResponse.class
                );
            } else {
                response = restTemplate.postForEntity(baseUrl, request, GoogleCalendarEventResponse.class);
            }

            GoogleCalendarEventResponse body = response.getBody();
            if (body == null || !hasText(body.getId())) {
                throw new ServiceLogicException(
                        ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                        "Google Calendar 일정 응답이 비어 있습니다."
                );
            }

            return body;
        } catch (RestClientException exception) {
            log.warn("Google Calendar upsert failed for tutoringId={}: {}", tutoring.getTutoringId(), exception.getMessage());
            throw new ServiceLogicException(
                    ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                    "Google Calendar 일정 동기화에 실패했습니다."
            );
        }
    }

    public void deleteEvent(String eventId) {
        if (!hasText(eventId)) {
            return;
        }

        validateConfigured();

        String accessToken = refreshAccessToken();
        String url = String.format(
                "%s/calendars/%s/events/%s",
                calendarApiUrl,
                encodePath(calendarId),
                encodePath(eventId)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        } catch (HttpClientErrorException.NotFound ignored) {
            log.info("Google Calendar event already removed: {}", eventId);
        } catch (RestClientException exception) {
            log.warn("Google Calendar delete failed for eventId={}: {}", eventId, exception.getMessage());
            throw new ServiceLogicException(
                    ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                    "Google Calendar 일정 삭제에 실패했습니다."
            );
        }
    }

    private void validateConfigured() {
        if (!isConfigured()) {
            throw new ServiceLogicException(
                    ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                    getConfigurationErrorMessage()
            );
        }
    }

    private String refreshAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    authUrl + "/token",
                    request,
                    GoogleTokenResponse.class
            );
            GoogleTokenResponse payload = response.getBody();
            if (payload == null || !hasText(payload.getAccessToken())) {
                throw new ServiceLogicException(
                        ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                        "Google Calendar access token 발급에 실패했습니다."
                );
            }
            return payload.getAccessToken();
        } catch (RestClientException exception) {
            log.warn("Google Calendar token refresh failed: {}", exception.getMessage());
            throw new ServiceLogicException(
                    ErrorCode.GOOGLE_CALENDAR_SYNC_FAILED,
                    "Google Calendar access token 발급에 실패했습니다."
            );
        }
    }

    private Map<String, Object> buildEventPayload(Tutoring tutoring) {
        LocalDateTime startAt = LocalDateTime.of(
                tutoring.getDate().getYear(),
                tutoring.getDate().getMonth(),
                tutoring.getDate().getDayOfMonth(),
                tutoring.getHour(),
                tutoring.getMinute()
        );
        LocalDateTime endAt = startAt.plusMinutes(30);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", buildSummary(tutoring));
        payload.put("description", buildDescription(tutoring));
        payload.put("status", tutoring.getTutoringStatus() == TutoringStatus.CONFIRM ? "confirmed" : "tentative");
        payload.put("start", buildDateTimePayload(startAt));
        payload.put("end", buildDateTimePayload(endAt));
        payload.put("extendedProperties", Map.of(
                "private", Map.of("tutoringId", String.valueOf(tutoring.getTutoringId()))
        ));
        return payload;
    }

    private Map<String, String> buildDateTimePayload(LocalDateTime dateTime) {
        return Map.of(
                "dateTime", DATE_TIME_FORMATTER.format(dateTime),
                "timeZone", SEOUL_TIMEZONE
        );
    }

    private String buildSummary(Tutoring tutoring) {
        String prefix = tutoring.getTutoringStatus() == TutoringStatus.CONFIRM ? "[확정]" : "[대기]";
        return prefix + " 튜터링 예약 - " + safeValue(tutoring.getRequestUserNickName());
    }

    private String buildDescription(Tutoring tutoring) {
        String status = tutoring.getTutoringStatus() == TutoringStatus.CONFIRM ? "확정" : "대기";
        String detail = hasText(tutoring.getDetail()) ? tutoring.getDetail().trim() : "상세 요청 없음";
        return String.join("\n",
                "튜터링 ID: " + tutoring.getTutoringId(),
                "예약 상태: " + status,
                "학생 이름: " + safeValue(tutoring.getRequestUserNickName()),
                "학생 아이디: " + safeValue(tutoring.getRequestUsername()),
                String.format("예약 일시: %s %02d:%02d", tutoring.getDate(), tutoring.getHour(), tutoring.getMinute()),
                "",
                "상세 요청",
                detail
        );
    }

    private String safeValue(String value) {
        return hasText(value) ? value.trim() : "-";
    }

    private String encodePath(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

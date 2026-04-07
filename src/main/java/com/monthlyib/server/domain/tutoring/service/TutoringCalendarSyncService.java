package com.monthlyib.server.domain.tutoring.service;

import com.monthlyib.server.constant.TutoringStatus;
import com.monthlyib.server.domain.tutoring.dto.GoogleCalendarEventResponse;
import com.monthlyib.server.domain.tutoring.entity.Tutoring;
import com.monthlyib.server.domain.tutoring.repository.TutoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutoringCalendarSyncService {

    private final TutoringRepository tutoringRepository;
    private final GoogleCalendarService googleCalendarService;

    @Transactional
    public void syncTutoring(Long tutoringId) {
        Tutoring tutoring = tutoringRepository.findByTutoringId(tutoringId)
                .orElse(null);
        if (tutoring == null) {
            log.info("Skipped Google Calendar sync because tutoring was removed. tutoringId={}", tutoringId);
            return;
        }

        if (!googleCalendarService.isConfigured()) {
            tutoring.markGoogleCalendarFailed(googleCalendarService.getConfigurationErrorMessage());
            tutoringRepository.save(tutoring);
            return;
        }

        try {
            if (tutoring.getTutoringStatus() == TutoringStatus.CANCEL) {
                googleCalendarService.deleteEvent(tutoring.getGoogleCalendarEventId());
                tutoring.markGoogleCalendarDeleted();
            } else {
                GoogleCalendarEventResponse response = googleCalendarService.upsertTutoringEvent(tutoring);
                tutoring.markGoogleCalendarSynced(response.getId(), response.getHtmlLink());
            }
        } catch (Exception exception) {
            tutoring.markGoogleCalendarFailed(exception.getMessage());
            log.error("Failed to sync tutoring to Google Calendar. tutoringId={}", tutoringId, exception);
        }

        tutoringRepository.save(tutoring);
    }

    public void deleteDetachedEvent(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }

        if (!googleCalendarService.isConfigured()) {
            log.warn("Google Calendar is not configured. Skipping detached event delete for eventId={}", eventId);
            return;
        }

        try {
            googleCalendarService.deleteEvent(eventId);
        } catch (Exception exception) {
            log.error("Failed to delete detached Google Calendar event. eventId={}", eventId, exception);
        }
    }
}

package com.monthlyib.server.event;

import com.monthlyib.server.domain.tutoring.service.TutoringCalendarSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TutoringCalendarEventListener {

    private final TutoringCalendarSyncService tutoringCalendarSyncService;

    @Async("calendarTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSyncRequested(TutoringCalendarSyncEvent event) {
        tutoringCalendarSyncService.syncTutoring(event.getTutoringId());
    }

    @Async("calendarTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleteRequested(TutoringCalendarDeleteEvent event) {
        tutoringCalendarSyncService.deleteDetachedEvent(event.getEventId());
    }
}

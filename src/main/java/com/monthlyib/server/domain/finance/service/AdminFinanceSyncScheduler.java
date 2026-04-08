package com.monthlyib.server.domain.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminFinanceSyncScheduler {

    private final AdminFinanceSnapshotSyncService adminFinanceSnapshotSyncService;

    @EventListener(ApplicationReadyEvent.class)
    public void triggerInitialBackfill() {
        adminFinanceSnapshotSyncService.triggerInitialBackfillIfNeeded();
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void syncPreviousDay() {
        adminFinanceSnapshotSyncService.triggerScheduledDailySync();
    }
}

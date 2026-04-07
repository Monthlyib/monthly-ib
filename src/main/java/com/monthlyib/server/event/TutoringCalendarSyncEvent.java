package com.monthlyib.server.event;

import org.springframework.context.ApplicationEvent;

public class TutoringCalendarSyncEvent extends ApplicationEvent {

    private final Long tutoringId;

    public TutoringCalendarSyncEvent(Object source, Long tutoringId) {
        super(source);
        this.tutoringId = tutoringId;
    }

    public Long getTutoringId() {
        return tutoringId;
    }
}

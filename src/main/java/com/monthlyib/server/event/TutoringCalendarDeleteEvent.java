package com.monthlyib.server.event;

import org.springframework.context.ApplicationEvent;

public class TutoringCalendarDeleteEvent extends ApplicationEvent {

    private final String eventId;

    public TutoringCalendarDeleteEvent(Object source, String eventId) {
        super(source);
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}

package com.monthlyib.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class UserTutoringConfirmEvent extends ApplicationEvent {

    private String email;
    private String nickName;
    private LocalDate date;
    private int hour;
    private int minute;

    public UserTutoringConfirmEvent(Object source, String email, String nickName) {
        super(source);
        this.email = email;
        this.nickName = nickName;
    }

    public UserTutoringConfirmEvent(Object source, String email, String nickName, LocalDate date, int hour, int minute) {
        super(source);
        this.email = email;
        this.nickName = nickName;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
    }
}

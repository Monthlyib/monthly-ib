package com.monthlyib.server.event;

import com.monthlyib.server.domain.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegistrationEvent extends ApplicationEvent {

    private String email;

    public UserRegistrationEvent(Object source, String email) {
        super(source);
        this.email = email;
    }
}

package com.monthlyib.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserQuestionConfirmEvent extends ApplicationEvent {

    private String email;

    private String nickName;

    public UserQuestionConfirmEvent(Object source, String email, String nickName) {
        super(source);
        this.email = email;
        this.nickName = nickName;
    }
}

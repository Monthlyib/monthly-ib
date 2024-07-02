package com.monthlyib.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserSendEmailEvent extends ApplicationEvent {

    private String email;

    private String nickName;

    private String content;

    public UserSendEmailEvent(Object source, String email, String nickName, String content) {
        super(source);
        this.email = email;
        this.nickName = nickName;
        this.content = content;
    }
}

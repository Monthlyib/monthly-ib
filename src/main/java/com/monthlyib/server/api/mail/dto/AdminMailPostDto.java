package com.monthlyib.server.api.mail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminMailPostDto {

    private List<Long> targetUserId;
    private String subject;
    private String content;
}

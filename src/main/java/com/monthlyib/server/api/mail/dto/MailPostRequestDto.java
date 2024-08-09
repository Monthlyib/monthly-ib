package com.monthlyib.server.api.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailPostRequestDto {

    private List<Long> targetUserId;

    private String content;
}

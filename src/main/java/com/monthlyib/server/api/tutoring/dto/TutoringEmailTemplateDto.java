package com.monthlyib.server.api.tutoring.dto;

import com.monthlyib.server.constant.TutoringEmailRecipientMode;
import com.monthlyib.server.domain.tutoring.entity.TutoringEmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringEmailTemplateDto {

    private Long id;
    private String subject;
    private String bodyTemplate;
    private boolean active;
    private TutoringEmailRecipientMode recipientMode;
    private String recipientEmail;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static TutoringEmailTemplateDto of(TutoringEmailTemplate entity) {
        TutoringEmailTemplateDto dto = new TutoringEmailTemplateDto();
        dto.id = entity.getId();
        dto.subject = entity.getSubject();
        dto.bodyTemplate = entity.getBodyTemplate();
        dto.active = entity.isActive();
        dto.recipientMode = entity.getRecipientMode();
        dto.recipientEmail = entity.getRecipientEmail();
        dto.createAt = entity.getCreateAt();
        dto.updateAt = entity.getUpdateAt();
        return dto;
    }
}

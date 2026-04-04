package com.monthlyib.server.api.tutoring.dto;

import com.monthlyib.server.constant.TutoringEmailRecipientMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringEmailTemplatePatchDto {
    @NotBlank
    private String subject;
    @NotBlank
    private String bodyTemplate;
    private Boolean active;
    private TutoringEmailRecipientMode recipientMode;
    @Email
    private String recipientEmail;
}

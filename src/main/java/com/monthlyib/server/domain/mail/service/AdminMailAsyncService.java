package com.monthlyib.server.domain.mail.service;

import com.monthlyib.server.mail.EmailAttachment;
import com.monthlyib.server.mail.EmailInlineImage;
import com.monthlyib.server.mail.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMailAsyncService {

    private static final String ADMIN_NOTICE_TEMPLATE = "email-admin-notice";

    private final EmailSender emailSender;

    @Async("mailTaskExecutor")
    public void sendInBackground(
            List<AdminMailRecipient> recipients,
            String subject,
            String contentHtml,
            List<EmailAttachment> attachments,
            List<EmailInlineImage> inlineImages
    ) {
        for (AdminMailRecipient recipient : recipients) {
            try {
                Map<String, Object> templateVariables = new HashMap<>();
                templateVariables.put("recipientName", recipient.recipientName());

                emailSender.sendEmail(
                        new String[]{recipient.email()},
                        subject,
                        contentHtml,
                        ADMIN_NOTICE_TEMPLATE,
                        templateVariables,
                        attachments,
                        inlineImages
                );
            } catch (Exception exception) {
                log.error(
                        "Failed to send admin mail asynchronously. recipient={}, subject={}",
                        recipient.email(),
                        subject,
                        exception
                );
            }
        }
    }

    public record AdminMailRecipient(String email, String recipientName) {
    }
}

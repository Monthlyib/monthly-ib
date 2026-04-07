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
    private final AdminMailJobService adminMailJobService;

    @Async("mailTaskExecutor")
    public void sendInBackground(
            List<AdminMailDispatch> dispatches,
            String subject,
            String contentHtml,
            List<EmailAttachment> attachments,
            List<EmailInlineImage> inlineImages
    ) {
        for (AdminMailDispatch dispatch : dispatches) {
            try {
                Map<String, Object> templateVariables = new HashMap<>();
                templateVariables.put("recipientName", dispatch.recipientName());

                emailSender.sendEmail(
                        new String[]{dispatch.email()},
                        subject,
                        contentHtml,
                        ADMIN_NOTICE_TEMPLATE,
                        templateVariables,
                        attachments,
                        inlineImages
                );
                adminMailJobService.markSent(dispatch.jobId());
            } catch (Exception exception) {
                adminMailJobService.markFailed(dispatch.jobId(), exception.getMessage());
                log.error(
                        "Failed to send admin mail asynchronously. recipient={}, subject={}",
                        dispatch.email(),
                        subject,
                        exception
                );
            }
        }
    }

    public record AdminMailRecipient(Long targetUserId, String email, String recipientName) {
    }

    public record AdminMailDispatch(Long jobId, Long targetUserId, String email, String recipientName) {
    }
}

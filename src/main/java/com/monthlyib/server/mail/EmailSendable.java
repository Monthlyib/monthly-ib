package com.monthlyib.server.mail;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public interface EmailSendable {
    void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables,
            List<EmailAttachment> attachments,
            List<EmailInlineImage> inlineImages
    )
            throws InterruptedException;

    default void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables
    ) throws InterruptedException {
        send(
                to,
                subject,
                message,
                templateName,
                variables,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    default void send(String[] to, String subject, String message, String templateName) throws InterruptedException {
        send(
                to,
                subject,
                message,
                templateName,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    default void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            List<EmailAttachment> attachments
    ) throws InterruptedException {
        send(to, subject, message, templateName, Collections.emptyMap(), attachments, Collections.emptyList());
    }

    default void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables,
            List<EmailAttachment> attachments
    ) throws InterruptedException {
        send(to, subject, message, templateName, variables, attachments, Collections.emptyList());
    }
}

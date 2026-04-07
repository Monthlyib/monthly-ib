package com.monthlyib.server.mail.service;

import com.monthlyib.server.mail.EmailSendable;
import com.monthlyib.server.mail.EmailAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailSender {

    private final EmailSendable emailSendable;

    public void sendEmail(String[] to, String subject, String message, String templateName) throws MailSendException,
            InterruptedException {
        emailSendable.send(to, subject, message, templateName);
    }

    public void sendEmail(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables
    ) throws MailSendException, InterruptedException {
        emailSendable.send(to, subject, message, templateName, variables);
    }

    public void sendEmail(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables,
            List<EmailAttachment> attachments
    ) throws MailSendException, InterruptedException {
        emailSendable.send(to, subject, message, templateName, variables, attachments);
    }
}

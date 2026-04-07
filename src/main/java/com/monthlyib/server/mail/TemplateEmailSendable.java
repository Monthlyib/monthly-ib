package com.monthlyib.server.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Slf4j
public class TemplateEmailSendable implements EmailSendable {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final String defaultFrom;

    public TemplateEmailSendable(JavaMailSender javaMailSender, TemplateEngine templateEngine, String defaultFrom) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.defaultFrom = defaultFrom;
    }

    @Override
    public void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables,
            List<EmailAttachment> attachments,
            List<EmailInlineImage> inlineImages
    ) {
        try {
            Context context = new Context();
            context.setVariable("message", message);
            context.setVariable("messageHtml", message);
            context.setVariable("subject", subject);
            context.setVariable("recipientName", variables.getOrDefault("recipientName", ""));
            variables.forEach(context::setVariable);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper =
                    new MimeMessageHelper(
                            mimeMessage,
                            (attachments != null && !attachments.isEmpty())
                                    || (inlineImages != null && !inlineImages.isEmpty()),
                            "UTF-8"
                    );

            String html = templateEngine.process(templateName, context);
            mimeMessageHelper.setTo(to);
            if (defaultFrom != null && !defaultFrom.isBlank()) {
                mimeMessageHelper.setFrom(defaultFrom);
            }
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(html, true);

            if (attachments != null) {
                for (EmailAttachment attachment : attachments) {
                    mimeMessageHelper.addAttachment(
                            attachment.fileName(),
                            new ByteArrayResource(attachment.data()),
                            attachment.contentType()
                    );
                }
            }

            if (inlineImages != null) {
                for (EmailInlineImage inlineImage : inlineImages) {
                    mimeMessageHelper.addInline(
                            inlineImage.contentId(),
                            new ByteArrayResource(inlineImage.data()),
                            inlineImage.contentType()
                    );
                }
            }

            javaMailSender.send(mimeMessage);
            log.info("Sent Template email!");
        } catch (Exception e) {
            log.error("email send error: ", e);
            throw new MailSendException("Failed to send email", e);
        }
    }
}

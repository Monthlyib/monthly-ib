package com.monthlyib.server.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
public class TemplateEmailSendable implements EmailSendable {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public TemplateEmailSendable(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void send(
            String[] to,
            String subject,
            String message,
            String templateName,
            Map<String, Object> variables
    ) {
        try {
            Context context = new Context();
            context.setVariable("message", message);
            context.setVariable("subject", subject);
            context.setVariable("recipientName", variables.getOrDefault("recipientName", ""));
            variables.forEach(context::setVariable);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            String html = templateEngine.process(templateName, context);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(html, true);

            javaMailSender.send(mimeMessage);
            log.info("Sent Template email!");
        } catch (Exception e) {
            log.error("email send error: ", e);
            throw new MailSendException("Failed to send email", e);
        }
    }
}

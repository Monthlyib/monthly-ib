package com.monthlyib.server.mail;

public record EmailAttachment(
        String fileName,
        String contentType,
        byte[] data
) {
}

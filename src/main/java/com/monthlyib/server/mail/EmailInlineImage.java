package com.monthlyib.server.mail;

public record EmailInlineImage(
        String contentId,
        String fileName,
        String contentType,
        byte[] data
) {
}

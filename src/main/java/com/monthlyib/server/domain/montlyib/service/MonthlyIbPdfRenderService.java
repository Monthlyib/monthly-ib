package com.monthlyib.server.domain.montlyib.service;

import com.monthlyib.server.domain.montlyib.entity.MonthlyIb;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class MonthlyIbPdfRenderService {

    private static final String FONT_RESOURCE_PATH = "fonts/NanumGothic-Regular.ttf";
    private static final String PDF_STYLE = """
            @page {
              size: A4;
              margin: 26mm 20mm 24mm;
            }

            body {
              font-family: 'Nanum Gothic';
              color: #241b34;
              font-size: 12pt;
              line-height: 1.7;
            }

            .article {
              width: 100%;
            }

            .eyebrow {
              margin: 0 0 10px;
              color: #6c5a85;
              font-size: 10pt;
              font-weight: 700;
              letter-spacing: 0.18em;
              text-transform: uppercase;
            }

            h1 {
              margin: 0 0 18px;
              font-size: 24pt;
              line-height: 1.25;
              color: #2e2143;
            }

            .thumbnail {
              width: 100%;
              margin: 0 0 18px;
              border-radius: 14px;
            }

            .body {
              width: 100%;
            }

            .body img {
              display: block;
              max-width: 100%;
              height: auto;
              margin: 16px auto;
              border-radius: 12px;
            }

            .body p, .body li, .body blockquote {
              margin: 0 0 12px;
            }

            .body h1, .body h2, .body h3 {
              margin: 22px 0 12px;
              color: #2e2143;
              page-break-after: avoid;
            }

            .body h2 {
              font-size: 17pt;
            }

            .body h3 {
              font-size: 14pt;
            }

            .body a {
              color: #5a3b81;
              text-decoration: underline;
            }

            .body blockquote {
              padding: 12px 16px;
              border-left: 4px solid #c6b4df;
              background: #f8f4fc;
              color: #4a3c60;
            }

            .body ul, .body ol {
              padding-left: 22px;
              margin: 0 0 14px;
            }

            .body .ql-align-center {
              text-align: center;
            }

            .body .ql-align-right {
              text-align: right;
            }

            .body .ql-align-justify {
              text-align: justify;
            }

            .body .ql-size-large {
              font-size: 1.2em;
            }

            .body .ql-size-huge {
              font-size: 1.5em;
            }

            .body .ql-indent-1 {
              padding-left: 2em;
            }

            .body .ql-indent-2 {
              padding-left: 4em;
            }
            """;

    public byte[] render(MonthlyIb monthlyIb) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Path tempFontPath = copyFontToTempFile();
            try {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.useFont(tempFontPath.toFile(), "Nanum Gothic");
                builder.withW3cDocument(new W3CDom().fromJsoup(buildHtmlDocument(monthlyIb)), null);
                builder.toStream(outputStream);
                builder.run();
                return outputStream.toByteArray();
            } finally {
                Files.deleteIfExists(tempFontPath);
            }
        } catch (Exception exception) {
            log.error("Failed to render Monthly IB pdf", exception);
            throw new IllegalStateException("Failed to render Monthly IB pdf", exception);
        }
    }

    private Path copyFontToTempFile() throws IOException {
        ClassPathResource fontResource = new ClassPathResource(FONT_RESOURCE_PATH);
        Path tempFile = Files.createTempFile("monthly-ib-font-", ".ttf");
        try (InputStream inputStream = fontResource.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private Document buildHtmlDocument(MonthlyIb monthlyIb) {
        Document document = Document.createShell("");
        document.outputSettings().prettyPrint(false);
        document.head().appendElement("meta").attr("charset", "UTF-8");
        document.head().appendElement("style").append(PDF_STYLE);

        Element article = document.body().appendElement("article").addClass("article");
        article.appendElement("p").addClass("eyebrow").text("Monthly IB");
        article.appendElement("h1").text(monthlyIb.getTitle() == null ? "" : monthlyIb.getTitle());

        if (monthlyIb.getMonthlyIbThumbnailFileUrl() != null && !monthlyIb.getMonthlyIbThumbnailFileUrl().isBlank()) {
            article.appendElement("img")
                    .addClass("thumbnail")
                    .attr("src", monthlyIb.getMonthlyIbThumbnailFileUrl())
                    .attr("alt", monthlyIb.getTitle() == null ? "Monthly IB thumbnail" : monthlyIb.getTitle());
        }

        Element body = article.appendElement("section").addClass("body");
        body.html(monthlyIb.getContent() == null ? "" : monthlyIb.getContent());

        return document;
    }
}

package com.monthlyib.server.domain.aiio.service;

import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
public class ScriptTextExtractionService {

    private static final int MAX_REFERENCE_TEXT_LENGTH = 15_000;

    public String extract(MultipartFile scriptFile) {
        if (scriptFile == null || scriptFile.isEmpty()) {
            throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_REQUIRED);
        }

        String extension = extractExtension(scriptFile.getOriginalFilename());
        String text = switch (extension) {
            case "txt" -> extractTextFile(scriptFile);
            case "pdf" -> extractPdf(scriptFile);
            case "docx" -> extractDocx(scriptFile);
            case "doc" -> throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_UNSUPPORTED);
            default -> throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_UNSUPPORTED);
        };

        String normalized = normalize(text);
        if (normalized.isBlank()) {
            throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_EMPTY);
        }

        return normalized.length() > MAX_REFERENCE_TEXT_LENGTH
                ? normalized.substring(0, MAX_REFERENCE_TEXT_LENGTH)
                : normalized;
    }

    private String extractTextFile(MultipartFile scriptFile) {
        try {
            return new String(scriptFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }
    }

    private String extractPdf(MultipartFile scriptFile) {
        try (PDDocument document = PDDocument.load(scriptFile.getInputStream())) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            log.warn("Failed to extract PDF text", e);
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }
    }

    private String extractDocx(MultipartFile scriptFile) {
        try (XWPFDocument document = new XWPFDocument(scriptFile.getInputStream())) {
            StringBuilder builder = new StringBuilder();
            document.getParagraphs().forEach(paragraph -> builder.append(paragraph.getText()).append('\n'));
            document.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell -> builder.append(cell.getText()).append('\n'))
                    )
            );
            return builder.toString();
        } catch (IOException e) {
            log.warn("Failed to extract DOCX text", e);
            throw new ServiceLogicException(ErrorCode.FILE_CONVERT_ERROR);
        }
    }

    private String normalize(String value) {
        return Optional.ofNullable(value)
                .orElse("")
                .replace('\u00A0', ' ')
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll(" +", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ServiceLogicException(ErrorCode.AI_IO_SCRIPT_UNSUPPORTED);
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

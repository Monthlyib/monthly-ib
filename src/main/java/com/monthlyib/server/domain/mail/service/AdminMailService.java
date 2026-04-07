package com.monthlyib.server.domain.mail.service;

import com.monthlyib.server.api.mail.dto.AdminMailPostDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.mail.EmailAttachment;
import com.monthlyib.server.mail.EmailInlineImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMailService {

    private static final int MAX_ATTACHMENT_COUNT = 5;
    private static final long MAX_TOTAL_ATTACHMENT_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif",
            "pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip"
    );
    private static final Set<String> ALLOWED_INLINE_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );
    private static final String INLINE_IMAGE_STYLE =
            "display:block;max-width:100%;height:auto;margin:16px 0;border-radius:14px;";
    private static final String PARAGRAPH_STYLE =
            "margin:0 0 16px;font-size:16px;line-height:1.8;color:#241b34;";
    private static final String LIST_STYLE =
            "margin:0 0 16px 24px;padding:0;font-size:16px;line-height:1.8;color:#241b34;";
    private static final String BLOCKQUOTE_STYLE =
            "margin:0 0 16px;padding:12px 18px;border-left:4px solid #d9caea;background:#f8f3ff;color:#4a385f;";
    private static final String LINK_STYLE = "color:#51346c;text-decoration:underline;";

    private final UserRepository userRepository;
    private final AdminMailAsyncService adminMailAsyncService;

    public Map<String, Object> send(
            AdminMailPostDto requestDto,
            MultipartFile[] attachments,
            MultipartFile[] inlineImages,
            User adminUser
    ) {
        verifyAdmin(adminUser);

        List<Long> targetIds = normalizeTargetIds(requestDto);
        String subject = normalizeSubject(requestDto);
        NormalizedMailPayload payload = normalizePayload(requestDto, attachments, inlineImages);

        List<User> targetUsers = targetIds.stream()
                .map(userRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(user -> UserStatus.ACTIVE.equals(user.getUserStatus()))
                .toList();

        if (targetUsers.isEmpty()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_RECIPIENT_NOT_FOUND,
                    "메일을 보낼 수 있는 활성 사용자를 찾지 못했습니다."
            );
        }

        for (User targetUser : targetUsers) {
            if (targetUser.getEmail() == null || targetUser.getEmail().isBlank()) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_RECIPIENT_EMAIL_NOT_FOUND,
                        "선택한 사용자에게 등록된 이메일 주소가 없습니다."
                );
            }
        }

        List<AdminMailAsyncService.AdminMailRecipient> recipients = targetUsers.stream()
                .map(targetUser -> new AdminMailAsyncService.AdminMailRecipient(
                        targetUser.getEmail().trim(),
                        getRecipientName(targetUser)
                ))
                .toList();

        adminMailAsyncService.sendInBackground(
                recipients,
                subject,
                payload.contentHtml(),
                payload.attachments(),
                payload.inlineImages()
        );

        return Map.of(
                "queuedCount", targetUsers.size(),
                "targetUserId", targetUsers.stream().map(User::getUserId).toList(),
                "attachmentCount", payload.attachments().size(),
                "inlineImageCount", payload.inlineImages().size(),
                "deliveryStatus", "QUEUED"
        );
    }

    private NormalizedMailPayload normalizePayload(
            AdminMailPostDto requestDto,
            MultipartFile[] attachments,
            MultipartFile[] inlineImages
    ) {
        List<MultipartFile> attachmentFiles = normalizeFiles(attachments);
        List<MultipartFile> inlineImageFiles = normalizeFiles(inlineImages);

        validateFileBudget(attachmentFiles, inlineImageFiles);

        List<String> inlineImageIds = normalizeInlineImageIds(requestDto, inlineImageFiles.size());
        String contentHtml = normalizeContentHtml(requestDto, inlineImageIds);

        List<EmailAttachment> normalizedAttachments = normalizeAttachments(attachmentFiles);
        List<EmailInlineImage> normalizedInlineImages = normalizeInlineImages(inlineImageFiles, inlineImageIds);

        return new NormalizedMailPayload(contentHtml, normalizedAttachments, normalizedInlineImages);
    }

    private List<MultipartFile> normalizeFiles(MultipartFile[] files) {
        return files == null
                ? List.of()
                : Arrays.stream(files)
                .filter(Objects::nonNull)
                .filter(file -> file.getOriginalFilename() != null || !file.isEmpty())
                .toList();
    }

    private void validateFileBudget(List<MultipartFile> attachments, List<MultipartFile> inlineImages) {
        int totalFileCount = attachments.size() + inlineImages.size();
        if (totalFileCount > MAX_ATTACHMENT_COUNT) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_ATTACHMENT_COUNT_EXCEEDED,
                    "본문 이미지와 첨부파일은 합쳐서 최대 5개까지 보낼 수 있습니다."
            );
        }

        long totalSize = 0L;
        for (MultipartFile file : attachments) {
            validateNonEmptyFile(file);
            totalSize += file.getSize();
            if (totalSize > MAX_TOTAL_ATTACHMENT_BYTES) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_SIZE_EXCEEDED,
                        "본문 이미지와 첨부파일 총 용량은 10MB를 초과할 수 없습니다."
                );
            }
        }

        for (MultipartFile file : inlineImages) {
            validateNonEmptyFile(file);
            totalSize += file.getSize();
            if (totalSize > MAX_TOTAL_ATTACHMENT_BYTES) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_SIZE_EXCEEDED,
                        "본문 이미지와 첨부파일 총 용량은 10MB를 초과할 수 없습니다."
                );
            }
        }
    }

    private void validateNonEmptyFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() <= 0) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_ATTACHMENT_EMPTY,
                    "빈 첨부파일은 보낼 수 없습니다."
            );
        }
    }

    private List<EmailAttachment> normalizeAttachments(List<MultipartFile> files) {
        if (files.isEmpty()) {
            return List.of();
        }

        List<EmailAttachment> normalizedAttachments = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_TYPE_NOT_ALLOWED,
                        "허용되지 않은 첨부파일 형식입니다: " + safeFileName(originalFilename)
                );
            }

            try {
                normalizedAttachments.add(new EmailAttachment(
                        safeFileName(originalFilename),
                        normalizeContentType(file.getContentType(), extension),
                        file.getBytes()
                ));
            } catch (IOException e) {
                log.error("Failed to read admin mail attachment", e);
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_READ_FAILED,
                        "첨부파일을 읽는 중 오류가 발생했습니다. 다시 시도해주세요."
                );
            }
        }
        return List.copyOf(normalizedAttachments);
    }

    private List<EmailInlineImage> normalizeInlineImages(List<MultipartFile> files, List<String> inlineImageIds) {
        if (files.isEmpty()) {
            return List.of();
        }

        List<EmailInlineImage> normalizedInlineImages = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            if (!ALLOWED_INLINE_IMAGE_EXTENSIONS.contains(extension)) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_TYPE_NOT_ALLOWED,
                        "본문에 삽입할 수 없는 이미지 형식입니다: " + safeFileName(originalFilename)
                );
            }

            try {
                normalizedInlineImages.add(new EmailInlineImage(
                        inlineImageIds.get(index),
                        safeFileName(originalFilename),
                        normalizeContentType(file.getContentType(), extension),
                        file.getBytes()
                ));
            } catch (IOException e) {
                log.error("Failed to read inline admin mail image", e);
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_READ_FAILED,
                        "본문 이미지를 읽는 중 오류가 발생했습니다. 다시 시도해주세요."
                );
            }
        }
        return List.copyOf(normalizedInlineImages);
    }

    private List<String> normalizeInlineImageIds(AdminMailPostDto requestDto, int inlineImageCount) {
        List<String> inlineImageIds = requestDto == null || requestDto.getInlineImageIds() == null
                ? List.of()
                : requestDto.getInlineImageIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .toList();

        if (inlineImageIds.size() != inlineImageCount) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_INLINE_IMAGE_MAPPING_INVALID,
                    "본문 이미지 정보가 올바르지 않습니다. 이미지를 다시 첨부해주세요."
            );
        }

        if (new LinkedHashSet<>(inlineImageIds).size() != inlineImageIds.size()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_INLINE_IMAGE_MAPPING_INVALID,
                    "본문 이미지 식별자가 중복되었습니다. 이미지를 다시 첨부해주세요."
            );
        }

        return List.copyOf(inlineImageIds);
    }

    private String normalizeContentHtml(AdminMailPostDto requestDto, List<String> inlineImageIds) {
        String content = requestDto == null ? null : requestDto.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_CONTENT_REQUIRED,
                    "메일 본문을 입력해주세요."
            );
        }

        Document document = Jsoup.parseBodyFragment(content.trim());
        document.outputSettings().prettyPrint(false);

        Set<String> allowedInlineImageIds = Set.copyOf(inlineImageIds);
        for (Element imageElement : document.select("img")) {
            String inlineImageId = imageElement.attr("data-inline-image-id").trim();
            if (inlineImageId.isBlank() || !allowedInlineImageIds.contains(inlineImageId)) {
                imageElement.remove();
                continue;
            }
            imageElement.attr("src", "cid:" + inlineImageId);
            imageElement.attr("style", INLINE_IMAGE_STYLE);
            imageElement.attr("alt", imageElement.attr("alt").isBlank() ? "메일 이미지" : imageElement.attr("alt"));
            imageElement.removeAttr("width");
            imageElement.removeAttr("height");
            imageElement.removeAttr("srcset");
        }

        boolean hasTextContent = !document.text().replace('\u00A0', ' ').trim().isEmpty();
        boolean hasInlineImages = !document.select("img").isEmpty();
        if (!hasTextContent && !hasInlineImages) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_CONTENT_REQUIRED,
                    "메일 본문을 입력해주세요."
            );
        }

        for (Element paragraph : document.select("p")) {
            paragraph.attr("style", PARAGRAPH_STYLE);
        }
        for (Element block : document.select("div")) {
            block.attr("style", PARAGRAPH_STYLE);
        }
        for (Element unorderedList : document.select("ul,ol")) {
            unorderedList.attr("style", LIST_STYLE);
        }
        for (Element listItem : document.select("li")) {
            listItem.attr("style", "margin:0 0 8px;");
        }
        for (Element blockQuote : document.select("blockquote")) {
            blockQuote.attr("style", BLOCKQUOTE_STYLE);
        }
        for (Element link : document.select("a[href]")) {
            link.attr("target", "_blank");
            link.attr("rel", "noreferrer noopener");
            link.attr("style", LINK_STYLE);
        }
        for (Element heading : document.select("h1,h2,h3,h4")) {
            heading.attr("style", "margin:0 0 16px;color:#241b34;line-height:1.45;");
        }

        String cleaned = Jsoup.clean(
                document.body().html(),
                "",
                buildMailSafelist(),
                new Document.OutputSettings().prettyPrint(false)
        );

        return cleaned.trim();
    }

    private Safelist buildMailSafelist() {
        return Safelist.relaxed()
                .addTags("div", "span")
                .addAttributes(":all", "style")
                .addAttributes("a", "target", "rel")
                .addAttributes("img", "data-inline-image-id", "alt")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "cid");
    }

    private String extractExtension(String filename) {
        String safeName = safeFileName(filename);
        int extensionIndex = safeName.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == safeName.length() - 1) {
            return "";
        }
        return safeName.substring(extensionIndex + 1).toLowerCase();
    }

    private String safeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "attachment";
        }
        return filename.replaceAll("[\\r\\n]", "_").trim();
    }

    private String normalizeContentType(String contentType, String extension) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    private List<Long> normalizeTargetIds(AdminMailPostDto requestDto) {
        List<Long> targetIds = requestDto == null || requestDto.getTargetUserId() == null
                ? List.of()
                : requestDto.getTargetUserId().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));

        if (targetIds.isEmpty()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_TARGET_USER_REQUIRED,
                    "메일을 보낼 사용자를 선택해주세요."
            );
        }
        return targetIds;
    }

    private String normalizeSubject(AdminMailPostDto requestDto) {
        String subject = requestDto == null ? null : requestDto.getSubject();
        if (subject == null || subject.trim().isEmpty()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_SUBJECT_REQUIRED,
                    "메일 제목을 입력해주세요."
            );
        }
        return subject.trim();
    }

    private String getRecipientName(User user) {
        if (user.getNickName() != null && !user.getNickName().isBlank()) {
            return user.getNickName();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return "회원";
    }

    private void verifyAdmin(User user) {
        if (user == null || !Authority.ADMIN.equals(user.getAuthority())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private record NormalizedMailPayload(
            String contentHtml,
            List<EmailAttachment> attachments,
            List<EmailInlineImage> inlineImages
    ) {
    }
}

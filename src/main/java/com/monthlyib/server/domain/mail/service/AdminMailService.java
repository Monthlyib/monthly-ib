package com.monthlyib.server.domain.mail.service;

import com.monthlyib.server.api.mail.dto.AdminMailPostDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.mail.EmailAttachment;
import com.monthlyib.server.mail.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMailService {

    private static final String ADMIN_NOTICE_TEMPLATE = "email-admin-notice";
    private static final int MAX_ATTACHMENT_COUNT = 5;
    private static final long MAX_TOTAL_ATTACHMENT_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif",
            "pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip"
    );

    private final UserRepository userRepository;
    private final EmailSender emailSender;

    public Map<String, Object> send(AdminMailPostDto requestDto, MultipartFile[] attachments, User adminUser) {
        verifyAdmin(adminUser);

        List<Long> targetIds = normalizeTargetIds(requestDto);
        String subject = normalizeSubject(requestDto);
        String content = normalizeContent(requestDto);
        List<EmailAttachment> normalizedAttachments = normalizeAttachments(attachments);

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

        try {
            for (User targetUser : targetUsers) {
                Map<String, Object> templateVariables = new HashMap<>();
                templateVariables.put("recipientName", getRecipientName(targetUser));

                emailSender.sendEmail(
                        new String[]{targetUser.getEmail().trim()},
                        subject,
                        content,
                        ADMIN_NOTICE_TEMPLATE,
                        templateVariables,
                        normalizedAttachments
                );
            }
        } catch (MailSendException e) {
            log.error("Failed to send admin mail. targets={}, subject={}", targetIds, subject, e);
            throw new ServiceLogicException(
                    ErrorCode.MAIL_SEND_FAILED,
                    "메일 전송에 실패했습니다. 메일 서버 설정 또는 수신자 정보를 확인해주세요."
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceLogicException(
                    ErrorCode.MAIL_SEND_FAILED,
                    "메일 전송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );
        } catch (RuntimeException e) {
            log.error("Unexpected admin mail error. targets={}, subject={}", targetIds, subject, e);
            throw new ServiceLogicException(
                    ErrorCode.MAIL_SEND_FAILED,
                    "메일 전송에 실패했습니다. 수신자 정보와 메일 템플릿을 확인해주세요."
            );
        }

        return Map.of(
                "sentCount", targetUsers.size(),
                "targetUserId", targetUsers.stream().map(User::getUserId).toList(),
                "attachmentCount", normalizedAttachments.size()
        );
    }

    private List<EmailAttachment> normalizeAttachments(MultipartFile[] attachments) {
        List<MultipartFile> normalizedFiles = attachments == null
                ? List.of()
                : Arrays.stream(attachments)
                .filter(Objects::nonNull)
                .filter(file -> file.getOriginalFilename() != null || !file.isEmpty())
                .toList();

        if (normalizedFiles.isEmpty()) {
            return List.of();
        }

        if (normalizedFiles.size() > MAX_ATTACHMENT_COUNT) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_ATTACHMENT_COUNT_EXCEEDED,
                    "첨부파일은 최대 5개까지 보낼 수 있습니다."
            );
        }

        long totalSize = 0L;
        for (MultipartFile file : normalizedFiles) {
            if (file.isEmpty() || file.getSize() <= 0) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_EMPTY,
                        "빈 첨부파일은 보낼 수 없습니다."
                );
            }

            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_TYPE_NOT_ALLOWED,
                        "허용되지 않은 첨부파일 형식입니다: " + safeFileName(originalFilename)
                );
            }

            totalSize += file.getSize();
            if (totalSize > MAX_TOTAL_ATTACHMENT_BYTES) {
                throw new ServiceLogicException(
                        ErrorCode.MAIL_ATTACHMENT_SIZE_EXCEEDED,
                        "첨부파일 총 용량은 10MB를 초과할 수 없습니다."
                );
            }
        }

        List<EmailAttachment> normalizedAttachments = new ArrayList<>();
        for (MultipartFile file : normalizedFiles) {
            try {
                normalizedAttachments.add(new EmailAttachment(
                        safeFileName(file.getOriginalFilename()),
                        normalizeContentType(file.getContentType()),
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

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    private List<Long> normalizeTargetIds(AdminMailPostDto requestDto) {
        List<Long> targetIds = requestDto == null || requestDto.getTargetUserId() == null
                ? List.of()
                : requestDto.getTargetUserId().stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
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

    private String normalizeContent(AdminMailPostDto requestDto) {
        String content = requestDto == null ? null : requestDto.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new ServiceLogicException(
                    ErrorCode.MAIL_CONTENT_REQUIRED,
                    "메일 본문을 입력해주세요."
            );
        }
        return content.trim();
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
}

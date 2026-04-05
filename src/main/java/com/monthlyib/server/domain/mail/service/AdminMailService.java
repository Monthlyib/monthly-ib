package com.monthlyib.server.domain.mail.service;

import com.monthlyib.server.api.mail.dto.AdminMailPostDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.constant.UserStatus;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.domain.user.repository.UserRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import com.monthlyib.server.mail.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMailService {

    private static final String ADMIN_NOTICE_TEMPLATE = "email-admin-notice";

    private final UserRepository userRepository;
    private final EmailSender emailSender;

    public Map<String, Object> send(AdminMailPostDto requestDto, User adminUser) {
        verifyAdmin(adminUser);

        List<Long> targetIds = normalizeTargetIds(requestDto);
        String subject = normalizeSubject(requestDto);
        String content = normalizeContent(requestDto);

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
                emailSender.sendEmail(
                        new String[]{targetUser.getEmail().trim()},
                        subject,
                        content,
                        ADMIN_NOTICE_TEMPLATE,
                        Map.of("recipientName", getRecipientName(targetUser))
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
        }

        return Map.of(
                "sentCount", targetUsers.size(),
                "targetUserId", targetUsers.stream().map(User::getUserId).toList()
        );
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
        return user.getUsername();
    }

    private void verifyAdmin(User user) {
        if (user == null || !Authority.ADMIN.equals(user.getAuthority())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }
}

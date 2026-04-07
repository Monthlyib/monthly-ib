package com.monthlyib.server.domain.mail.service;

import com.monthlyib.server.api.mail.dto.AdminMailJobResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.mail.entity.AdminMailJob;
import com.monthlyib.server.domain.mail.repository.AdminMailJobJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMailJobService {

    private final AdminMailJobJpaRepository adminMailJobJpaRepository;

    public List<AdminMailJob> createQueuedJobs(
            List<AdminMailAsyncService.AdminMailRecipient> recipients,
            Long requestedByUserId,
            String subject,
            int attachmentCount,
            int inlineImageCount
    ) {
        List<AdminMailJob> jobs = recipients.stream()
                .map(recipient -> AdminMailJob.createQueued(
                        requestedByUserId,
                        recipient.targetUserId(),
                        recipient.email(),
                        recipient.recipientName(),
                        subject,
                        attachmentCount,
                        inlineImageCount
                ))
                .toList();

        return adminMailJobJpaRepository.saveAll(jobs);
    }

    public void markSent(Long jobId) {
        AdminMailJob job = adminMailJobJpaRepository.findById(jobId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        job.markSent();
        adminMailJobJpaRepository.save(job);
    }

    public void markFailed(Long jobId, String errorMessage) {
        AdminMailJob job = adminMailJobJpaRepository.findById(jobId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        job.markFailed(normalizeErrorMessage(errorMessage));
        adminMailJobJpaRepository.save(job);
    }

    public List<AdminMailJobResponseDto> findRecentJobs(User adminUser) {
        verifyAdmin(adminUser);
        return adminMailJobJpaRepository.findTop20ByOrderByCreateAtDesc().stream()
                .map(AdminMailJobResponseDto::of)
                .toList();
    }

    private String normalizeErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "메일 전송 중 알 수 없는 오류가 발생했습니다.";
        }
        if (errorMessage.length() <= 1000) {
            return errorMessage;
        }
        return errorMessage.substring(0, 1000);
    }

    private void verifyAdmin(User user) {
        if (user == null || !Authority.ADMIN.equals(user.getAuthority())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }
}

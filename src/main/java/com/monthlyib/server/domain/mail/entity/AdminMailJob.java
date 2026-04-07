package com.monthlyib.server.domain.mail.entity;

import com.monthlyib.server.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Table(name = "admin_mail_job")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMailJob extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestedByUserId;

    @Column(nullable = true)
    private Long targetUserId;

    @Column(nullable = false, length = 255)
    private String targetEmail;

    @Column(nullable = false, length = 120)
    private String targetName;

    @Column(nullable = false, length = 255)
    private String subject;

    @Builder.Default
    @Column(nullable = false)
    private int attachmentCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int inlineImageCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminMailJobStatus status;

    @Column(nullable = true, length = 1000)
    private String errorMessage;

    public static AdminMailJob createQueued(
            Long requestedByUserId,
            Long targetUserId,
            String targetEmail,
            String targetName,
            String subject,
            int attachmentCount,
            int inlineImageCount
    ) {
        return AdminMailJob.builder()
                .requestedByUserId(requestedByUserId)
                .targetUserId(targetUserId)
                .targetEmail(targetEmail)
                .targetName(targetName)
                .subject(subject)
                .attachmentCount(attachmentCount)
                .inlineImageCount(inlineImageCount)
                .status(AdminMailJobStatus.QUEUED)
                .build();
    }

    public void markSent() {
        this.status = AdminMailJobStatus.SENT;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = AdminMailJobStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}

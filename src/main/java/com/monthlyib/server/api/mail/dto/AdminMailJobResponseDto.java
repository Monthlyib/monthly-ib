package com.monthlyib.server.api.mail.dto;

import com.monthlyib.server.domain.mail.entity.AdminMailJob;
import com.monthlyib.server.domain.mail.entity.AdminMailJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMailJobResponseDto {

    private Long id;
    private Long requestedByUserId;
    private Long targetUserId;
    private String targetEmail;
    private String targetName;
    private String subject;
    private int attachmentCount;
    private int inlineImageCount;
    private AdminMailJobStatus status;
    private String errorMessage;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static AdminMailJobResponseDto of(AdminMailJob entity) {
        return AdminMailJobResponseDto.builder()
                .id(entity.getId())
                .requestedByUserId(entity.getRequestedByUserId())
                .targetUserId(entity.getTargetUserId())
                .targetEmail(entity.getTargetEmail())
                .targetName(entity.getTargetName())
                .subject(entity.getSubject())
                .attachmentCount(entity.getAttachmentCount())
                .inlineImageCount(entity.getInlineImageCount())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .createAt(entity.getCreateAt())
                .updateAt(entity.getUpdateAt())
                .build();
    }
}

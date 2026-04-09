package com.monthlyib.server.domain.aihistory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monthlyib.server.api.aihistory.dto.AiToolHistoryDetailResponseDto;
import com.monthlyib.server.api.aihistory.dto.AiToolHistorySummaryResponseDto;
import com.monthlyib.server.constant.Authority;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.aihistory.entity.AiToolHistory;
import com.monthlyib.server.domain.aihistory.entity.AiToolHistoryStatus;
import com.monthlyib.server.domain.aihistory.entity.AiToolType;
import com.monthlyib.server.domain.aihistory.model.AiToolHistoryCreateCommand;
import com.monthlyib.server.domain.aihistory.repository.AiToolHistoryJpaRepository;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiToolHistoryService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AiToolHistoryJpaRepository aiToolHistoryJpaRepository;
    private final ObjectMapper objectMapper;

    public void recordSuccess(AiToolHistoryCreateCommand command) {
        persist(command, AiToolHistoryStatus.SUCCESS);
    }

    public void recordFailure(AiToolHistoryCreateCommand command) {
        persist(command, AiToolHistoryStatus.FAILED);
    }

    @Transactional(readOnly = true)
    public Page<AiToolHistorySummaryResponseDto> findMyHistory(User user, String toolType, int page, int size) {
        User loginUser = requireUser(user);
        Pageable pageable = createPageable(page, size);
        AiToolType parsedToolType = parseToolType(toolType);

        Page<AiToolHistory> result = parsedToolType == null
                ? aiToolHistoryJpaRepository.findByUserUserId(loginUser.getUserId(), pageable)
                : aiToolHistoryJpaRepository.findByUserUserIdAndToolType(loginUser.getUserId(), parsedToolType, pageable);

        return result.map(AiToolHistorySummaryResponseDto::of);
    }

    @Transactional(readOnly = true)
    public AiToolHistoryDetailResponseDto findMyHistoryDetail(Long historyId, User user) {
        User loginUser = requireUser(user);
        AiToolHistory entity = aiToolHistoryJpaRepository.findByAiToolHistoryIdAndUserUserId(historyId, loginUser.getUserId())
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiToolHistoryDetailResponseDto.of(entity, readAttachmentUrls(entity.getAttachmentUrlsJson()));
    }

    @Transactional(readOnly = true)
    public Page<AiToolHistorySummaryResponseDto> findAdminHistory(Long userId, String toolType, int page, int size, User adminUser) {
        verifyAdmin(adminUser);
        Pageable pageable = createPageable(page, size);
        AiToolType parsedToolType = parseToolType(toolType);

        Page<AiToolHistory> result = parsedToolType == null
                ? aiToolHistoryJpaRepository.findByUserUserId(userId, pageable)
                : aiToolHistoryJpaRepository.findByUserUserIdAndToolType(userId, parsedToolType, pageable);

        return result.map(AiToolHistorySummaryResponseDto::of);
    }

    @Transactional(readOnly = true)
    public AiToolHistoryDetailResponseDto findAdminHistoryDetail(Long historyId, User adminUser) {
        verifyAdmin(adminUser);
        AiToolHistory entity = aiToolHistoryJpaRepository.findById(historyId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        return AiToolHistoryDetailResponseDto.of(entity, readAttachmentUrls(entity.getAttachmentUrlsJson()));
    }

    private void persist(AiToolHistoryCreateCommand command, AiToolHistoryStatus status) {
        if (command == null || command.getUser() == null) {
            return;
        }
        try {
            AiToolHistory entity = AiToolHistory.builder()
                    .user(command.getUser())
                    .toolType(command.getToolType())
                    .actionType(command.getActionType())
                    .status(status)
                    .title(command.getTitle() != null ? command.getTitle() : command.getActionType().name())
                    .summary(command.getSummary())
                    .subject(command.getSubject())
                    .chapter(command.getChapter())
                    .interestTopic(command.getInterestTopic())
                    .relatedEntityId(command.getRelatedEntityId())
                    .requestPayloadJson(toJson(command.getRequestPayload()))
                    .responsePayloadJson(toJson(command.getResponsePayload()))
                    .attachmentUrlsJson(toJson(command.getAttachmentUrls()))
                    .score(command.getScore())
                    .maxScore(command.getMaxScore())
                    .durationSeconds(command.getDurationSeconds())
                    .build();

            aiToolHistoryJpaRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to persist AI tool history. toolType={}, actionType={}, userId={}",
                    command.getToolType(),
                    command.getActionType(),
                    command.getUser().getUserId(),
                    e);
        }
    }

    private Pageable createPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createAt"));
    }

    private AiToolType parseToolType(String toolType) {
        if (toolType == null || toolType.isBlank()) {
            return null;
        }
        try {
            return AiToolType.valueOf(toolType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceLogicException(ErrorCode.BAD_REQUEST);
        }
    }

    private User requireUser(User user) {
        if (user == null) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
        return user;
    }

    private void verifyAdmin(User user) {
        if (user == null || !Authority.ADMIN.equals(user.getAuthority())) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED);
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize AI history payload: {}", e.getMessage());
            return String.valueOf(value);
        }
    }

    private List<String> readAttachmentUrls(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of(json);
        }
    }
}

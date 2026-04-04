package com.monthlyib.server.domain.tutoring.service;

import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplateDto;
import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplatePatchDto;
import com.monthlyib.server.constant.TutoringEmailRecipientMode;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.tutoring.entity.TutoringEmailTemplate;
import com.monthlyib.server.domain.tutoring.repository.TutoringEmailTemplateJpaRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TutoringEmailTemplateService {

    private static final String DEFAULT_RECIPIENT_EMAIL = "monthlyib@gmail.com";

    private final TutoringEmailTemplateJpaRepository repository;

    public List<TutoringEmailTemplateDto> findAll() {
        return repository.findAll().stream()
                .map(TutoringEmailTemplateDto::of)
                .toList();
    }

    public TutoringEmailTemplateDto findActive() {
        return repository.findFirstByActiveTrue()
                .map(this::ensureDefaults)
                .map(TutoringEmailTemplateDto::of)
                .orElseGet(() -> {
                    TutoringEmailTemplate def = TutoringEmailTemplate.createDefault();
                    return TutoringEmailTemplateDto.of(repository.save(def));
                });
    }

    public TutoringEmailTemplateDto create(TutoringEmailTemplatePatchDto dto) {
        TutoringEmailTemplate entity = TutoringEmailTemplate.builder()
                .subject(dto.getSubject())
                .bodyTemplate(dto.getBodyTemplate())
                .active(Optional.ofNullable(dto.getActive()).orElse(false))
                .recipientMode(resolveRecipientMode(dto.getRecipientMode()))
                .recipientEmail(resolveRecipientEmail(dto.getRecipientEmail()))
                .build();
        if (Boolean.TRUE.equals(dto.getActive())) {
            deactivateAll();
        }
        return TutoringEmailTemplateDto.of(repository.save(entity));
    }

    public TutoringEmailTemplateDto update(Long id, TutoringEmailTemplatePatchDto dto) {
        TutoringEmailTemplate entity = repository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        if (dto.getSubject() != null) entity.setSubject(dto.getSubject());
        if (dto.getBodyTemplate() != null) entity.setBodyTemplate(dto.getBodyTemplate());
        if (dto.getRecipientMode() != null) {
            entity.setRecipientMode(resolveRecipientMode(dto.getRecipientMode()));
        }
        if (dto.getRecipientEmail() != null) {
            entity.setRecipientEmail(resolveRecipientEmail(dto.getRecipientEmail()));
        }
        if (Boolean.TRUE.equals(dto.getActive())) {
            deactivateAll();
            entity.setActive(true);
        } else if (Boolean.FALSE.equals(dto.getActive())) {
            entity.setActive(false);
        }
        return TutoringEmailTemplateDto.of(repository.save(ensureDefaults(entity)));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public TutoringEmailTemplateDto activate(Long id) {
        deactivateAll();
        TutoringEmailTemplate entity = repository.findById(id)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND));
        entity.setActive(true);
        return TutoringEmailTemplateDto.of(repository.save(entity));
    }

    private void deactivateAll() {
        repository.findAll().forEach(t -> {
            t.setActive(false);
            repository.save(t);
        });
    }

    public TutoringEmailTemplate getActiveEntity() {
        return repository.findFirstByActiveTrue()
                .map(this::ensureDefaults)
                .orElseGet(() -> {
                    TutoringEmailTemplate def = TutoringEmailTemplate.createDefault();
                    return repository.save(def);
                });
    }

    private TutoringEmailTemplate ensureDefaults(TutoringEmailTemplate entity) {
        boolean changed = false;
        if (entity.getRecipientMode() == null) {
            entity.setRecipientMode(TutoringEmailRecipientMode.BOTH);
            changed = true;
        }
        if (!StringUtils.hasText(entity.getRecipientEmail())) {
            entity.setRecipientEmail(DEFAULT_RECIPIENT_EMAIL);
            changed = true;
        }
        if (changed) {
            return repository.save(entity);
        }
        return entity;
    }

    private TutoringEmailRecipientMode resolveRecipientMode(TutoringEmailRecipientMode mode) {
        return mode == null ? TutoringEmailRecipientMode.BOTH : mode;
    }

    private String resolveRecipientEmail(String recipientEmail) {
        return StringUtils.hasText(recipientEmail)
                ? recipientEmail.trim()
                : DEFAULT_RECIPIENT_EMAIL;
    }
}

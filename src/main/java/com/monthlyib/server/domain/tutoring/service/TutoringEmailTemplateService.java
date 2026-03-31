package com.monthlyib.server.domain.tutoring.service;

import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplateDto;
import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplatePatchDto;
import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.domain.tutoring.entity.TutoringEmailTemplate;
import com.monthlyib.server.domain.tutoring.repository.TutoringEmailTemplateJpaRepository;
import com.monthlyib.server.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TutoringEmailTemplateService {

    private final TutoringEmailTemplateJpaRepository repository;

    public List<TutoringEmailTemplateDto> findAll() {
        return repository.findAll().stream()
                .map(TutoringEmailTemplateDto::of)
                .toList();
    }

    public TutoringEmailTemplateDto findActive() {
        return repository.findFirstByActiveTrue()
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
        if (Boolean.TRUE.equals(dto.getActive())) {
            deactivateAll();
            entity.setActive(true);
        } else if (Boolean.FALSE.equals(dto.getActive())) {
            entity.setActive(false);
        }
        return TutoringEmailTemplateDto.of(repository.save(entity));
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
                .orElseGet(() -> {
                    TutoringEmailTemplate def = TutoringEmailTemplate.createDefault();
                    return repository.save(def);
                });
    }
}

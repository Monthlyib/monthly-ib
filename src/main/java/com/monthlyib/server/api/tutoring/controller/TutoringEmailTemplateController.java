package com.monthlyib.server.api.tutoring.controller;

import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplateDto;
import com.monthlyib.server.api.tutoring.dto.TutoringEmailTemplatePatchDto;
import com.monthlyib.server.domain.tutoring.service.TutoringEmailTemplateService;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutoring/email-template")
public class TutoringEmailTemplateController {

    private final TutoringEmailTemplateService service;

    @GetMapping
    public ResponseEntity<ResponseDto<?>> getAll() {
        List<TutoringEmailTemplateDto> list = service.findAll();
        return ResponseEntity.ok(ResponseDto.of(list, Result.ok()));
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseDto<?>> getActive() {
        TutoringEmailTemplateDto active = service.findActive();
        return ResponseEntity.ok(ResponseDto.of(active, Result.ok()));
    }

    @PostMapping
    public ResponseEntity<ResponseDto<?>> create(@RequestBody TutoringEmailTemplatePatchDto dto) {
        TutoringEmailTemplateDto created = service.create(dto);
        return ResponseEntity.ok(ResponseDto.of(created, Result.ok()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> update(@PathVariable Long id,
                                                  @RequestBody TutoringEmailTemplatePatchDto dto) {
        TutoringEmailTemplateDto updated = service.update(id, dto);
        return ResponseEntity.ok(ResponseDto.of(updated, Result.ok()));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ResponseDto<?>> activate(@PathVariable Long id) {
        TutoringEmailTemplateDto activated = service.activate(id);
        return ResponseEntity.ok(ResponseDto.of(activated, Result.ok()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<?>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ResponseDto.of(Result.ok()));
    }
}

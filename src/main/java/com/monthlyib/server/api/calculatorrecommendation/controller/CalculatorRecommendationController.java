package com.monthlyib.server.api.calculatorrecommendation.controller;

import com.monthlyib.server.api.calculatorrecommendation.dto.CalculatorRecommendationConfigDto;
import com.monthlyib.server.domain.calculatorrecommendation.service.CalculatorRecommendationService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CalculatorRecommendationController {

    private final CalculatorRecommendationService calculatorRecommendationService;

    @GetMapping("/open-api/calculator-recommendations")
    public ResponseEntity<ResponseDto<?>> getPublishedConfig() {
        return ResponseEntity.ok(ResponseDto.of(calculatorRecommendationService.getPublishedConfig(), Result.ok()));
    }

    @GetMapping("/api/admin/calculator-recommendations")
    public ResponseEntity<ResponseDto<?>> getAdminConfig() {
        return ResponseEntity.ok(ResponseDto.of(calculatorRecommendationService.getAdminConfig(), Result.ok()));
    }

    @PutMapping("/api/admin/calculator-recommendations")
    public ResponseEntity<ResponseDto<?>> saveConfig(@RequestBody CalculatorRecommendationConfigDto dto, User user) {
        return ResponseEntity.ok(ResponseDto.of(calculatorRecommendationService.saveConfig(dto, user), Result.ok()));
    }
}

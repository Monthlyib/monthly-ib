package com.monthlyib.server.api.finance.controller;

import com.monthlyib.server.annotation.UserSession;
import com.monthlyib.server.api.finance.dto.AdminFinanceDetailResponseDto;
import com.monthlyib.server.api.finance.dto.AdminFinanceOverviewResponseDto;
import com.monthlyib.server.api.finance.dto.FinanceSyncJobResponseDto;
import com.monthlyib.server.domain.finance.service.AdminFinanceService;
import com.monthlyib.server.domain.finance.service.AdminFinanceSnapshotSyncService;
import com.monthlyib.server.domain.user.entity.User;
import com.monthlyib.server.dto.ResponseDto;
import com.monthlyib.server.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {

    private final AdminFinanceService adminFinanceService;
    private final AdminFinanceSnapshotSyncService adminFinanceSnapshotSyncService;

    @GetMapping("/overview")
    public ResponseEntity<ResponseDto<?>> getOverview(
            @RequestParam(defaultValue = "12") int months,
            @UserSession User user
    ) {
        AdminFinanceOverviewResponseDto response = adminFinanceService.getOverview(user, months);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @GetMapping("/details")
    public ResponseEntity<ResponseDto<?>> getDetails(
            @RequestParam String yearMonth,
            @UserSession User user
    ) {
        AdminFinanceDetailResponseDto response = adminFinanceService.getDetails(user, yearMonth);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }

    @PostMapping("/sync")
    public ResponseEntity<ResponseDto<?>> triggerSync(@UserSession User user) {
        FinanceSyncJobResponseDto response = adminFinanceSnapshotSyncService.triggerManualSync(user);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}

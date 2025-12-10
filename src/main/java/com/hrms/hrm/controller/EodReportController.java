package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.EodRequestDto;
import com.hrms.hrm.dto.EodResponseDto;
import com.hrms.hrm.service.EodReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/eod")
@RequiredArgsConstructor
public class EodReportController {

    private final EodReportService eodReportService;

    @PostMapping
    public ResponseEntity<ApiResponse<EodResponseDto>> create(@RequestBody EodRequestDto request) {
        return ResponseEntity.ok(
                ApiResponse.success(eodReportService.createEod(request), "EOD Created")
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EodResponseDto>> update(
            @PathVariable UUID id,
            @RequestBody EodRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(eodReportService.updateEod(id, request), "EOD Updated")
        );
    }

    @GetMapping("/employee/{code}")
    public ResponseEntity<ApiResponse<List<EodResponseDto>>> getByEmployee(@PathVariable String code) {
        return ResponseEntity.ok(
                ApiResponse.success(eodReportService.getEmployeeEods(code), "Employee EODs Fetched")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EodResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(eodReportService.getAllEods(), "All EODs fetched")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        eodReportService.deleteEod(id);
        return ResponseEntity.ok(ApiResponse.success(null, "EOD Deleted"));
    }
}

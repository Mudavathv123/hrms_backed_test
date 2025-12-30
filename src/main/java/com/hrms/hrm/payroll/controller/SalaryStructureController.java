package com.hrms.hrm.payroll.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.payroll.dto.SalaryStructureRequest;
import com.hrms.hrm.payroll.dto.SalaryStructureResponse;
import com.hrms.hrm.payroll.service.SalaryStructureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payroll/salary-structure")
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @PostMapping
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> create(
            @RequestBody @Valid SalaryStructureRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(salaryStructureService.create(request), "Salary structured created succesfully"));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> getEmployeeById(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success(salaryStructureService.getByEmployeeId(employeeId), "employee salary structure"));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<SalaryStructureResponse>> update(@PathVariable UUID employeeId,
            @RequestBody SalaryStructureRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(salaryStructureService.update(employeeId, request), "salary structure updated"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalaryStructureResponse>>> getAll() {
        return ResponseEntity
                .ok(ApiResponse.success(salaryStructureService.getAll(), "fetched all employee salary structures"));
    }

}

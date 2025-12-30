package com.hrms.hrm.payroll.controller;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.payroll.dto.PayrollDashboardResponse;
import com.hrms.hrm.payroll.dto.PayrollDetailsResponse;
import com.hrms.hrm.payroll.model.PaySlip;
import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.Payroll.PayrollStatus;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.hrms.hrm.payroll.repository.PayrollDeductionRepository;
import com.hrms.hrm.payroll.repository.PayrollRepository;
import com.hrms.hrm.payroll.repository.PayslipRepository;
import com.hrms.hrm.payroll.service.PayrollService;
import com.hrms.hrm.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollRepository payrollRepository;
    private final PayslipRepository payslipRepository;
    private final PayrollDeductionRepository deductionRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/generate")
    public Payroll generatePayroll(
            @RequestParam UUID employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return payrollService.generatePayroll(employeeId, month, year);
    }

    @GetMapping("/{payrollId}")
    public ResponseEntity<ApiResponse<PayrollDetailsResponse>> getPayrollDetails(@PathVariable UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

        List<PayrollDeduction> deductions = deductionRepository.findByPayrollId(payrollId);

        PayrollDetailsResponse response = new PayrollDetailsResponse();

        response.setPayroll(payroll);
        response.setDeductions(deductions);

        return ResponseEntity.ok(ApiResponse.success(response, "fetched payroll details successfully"));
    }

    @GetMapping("/payslip/{payrollId}/download")
    public ResponseEntity<Resource> downloadPayslip(@PathVariable UUID payrollId) throws Exception {
        PaySlip paySlip = payslipRepository.findByPayrollId(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        Path filePath = Path.of(paySlip.getPdfUrl());

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filePath.getFileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/dashboard")
    public ApiResponse<PayrollDashboardResponse> dashboard(
            @RequestParam int month,
            @RequestParam int year) {

        long employees = employeeRepository.count();
        long payrolls = payrollRepository.count();
        long pending = payrollRepository.countByStatus(Payroll.PayrollStatus.PENDING_APPROVAL);
        BigDecimal totalPaid = Optional.ofNullable(payrollRepository.getTotalSalaryPaid(month, year))
                .orElse(BigDecimal.ZERO);

        return ApiResponse.success(
                new PayrollDashboardResponse(employees, payrolls, totalPaid, pending),
                "Payroll dashboard data");
    }

    @GetMapping("/employee/{employeeId}")
    public List<Payroll> getEmployeePayrolls(@PathVariable UUID employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    @PutMapping("/{payrollId}/submit")
    public ApiResponse<String> submitForApproval(@PathVariable UUID payrollId) {
        Payroll p = payrollRepository.findById(payrollId).orElseThrow();
        p.setStatus(PayrollStatus.PENDING_APPROVAL);
        payrollRepository.save(p);
        return ApiResponse.success("Submitted for approval");
    }

    @PutMapping("/{payrollId}/approve")
    public ApiResponse<String> approvePayroll(
            @PathVariable UUID payrollId,
            @RequestParam UUID approverId) {

        Payroll p = payrollRepository.findById(payrollId).orElseThrow();
        p.setStatus(PayrollStatus.APPROVED);
        p.setApprovedBy(approverId);
        p.setApprovedAt(LocalDateTime.now());
        payrollRepository.save(p);

        return ApiResponse.success("Payroll approved");
    }

    @GetMapping("/pending-approvals")
    public List<Payroll> getPendingApprovals() {
        return payrollRepository.findByStatus(PayrollStatus.PENDING_APPROVAL);
    }

}

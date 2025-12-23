package com.hrms.hrm.controller;

import com.hrms.hrm.model.Payroll;
import com.hrms.hrm.repository.PayrollRepository;
import com.hrms.hrm.service.PayrollService;
import com.hrms.hrm.service.PayslipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollRepository payrollRepo;
    private final PayslipService payslipService;

    @PostMapping("/generate")
    public Payroll generate(@RequestParam UUID employeeId,
                            @RequestParam int year,
                            @RequestParam int month) {
        return payrollService.generatePayroll(employeeId, year, month);
    }

    @PostMapping("/{id}/lock")
    public void lock(@PathVariable UUID id) {
        payrollService.lockPayroll(id);
    }

    @GetMapping("/month")
    public List<Payroll> getByMonth(@RequestParam int year,
                                    @RequestParam int month) {
        return payrollRepo.findByMonthAndYear(month, year);
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable UUID id) throws Exception {

        Payroll payroll = payrollRepo.findById(id).orElseThrow();
        byte[] pdf = payslipService.generatePayslip(payroll);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}

package com.hrms.hrm.payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDashboardResponse {
    private long totalEmployees;
    private long totalPayrolls;
    private BigDecimal totalSalaryPaid;
    private long pendingPayrolls;
}

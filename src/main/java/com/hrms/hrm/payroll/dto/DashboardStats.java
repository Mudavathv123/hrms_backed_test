package com.hrms.hrm.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStats {
    private long totalEmployees;
    private long totalPayrolls;
    private long pendingApprovals;
    private double totalPaid;
}

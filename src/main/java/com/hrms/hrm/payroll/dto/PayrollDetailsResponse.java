package com.hrms.hrm.payroll.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class PayrollDetailsResponse {

    private UUID payrollId;
    private UUID employeeId;

    private int month;
    private int year;

    private int workingDays;
    private int presentDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;

    private Earnings earnings;
    private Deductions deductions;

    private BigDecimal grossSalary;
    private BigDecimal netSalary;

    private String status;

    @Data
    public static class Earnings {
        private BigDecimal basic;
        private BigDecimal hra;
        private BigDecimal allowance;
    }

    @Data
    public static class Deductions {
        private BigDecimal tax;
        private BigDecimal pf;
        private BigDecimal lossOfPay;
        private BigDecimal total;
    }
}

package com.hrms.hrm.payroll.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SalaryStructureResponse {

    private UUID id;
    private UUID employeeId;

    private BigDecimal hra;
    private BigDecimal allowance;
    private BigDecimal basic;
    private BigDecimal pfPercent;
    private BigDecimal taxPercent;
}

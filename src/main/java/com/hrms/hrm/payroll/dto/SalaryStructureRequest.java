package com.hrms.hrm.payroll.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryStructureRequest {

    private String employeeId;

    @NotNull @Positive
    private BigDecimal basic;

    private BigDecimal  hra = BigDecimal.ZERO;
    private BigDecimal allowance = BigDecimal.ZERO;
    private BigDecimal pfPercent = BigDecimal.valueOf(12);
    private BigDecimal taxPercent = BigDecimal.ZERO;
}

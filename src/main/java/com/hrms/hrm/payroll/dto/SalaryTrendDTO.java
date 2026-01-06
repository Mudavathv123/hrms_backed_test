package com.hrms.hrm.payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaryTrendDTO {
    private String period;
    private BigDecimal salary;
    private BigDecimal tax;
}

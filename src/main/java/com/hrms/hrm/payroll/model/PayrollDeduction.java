package com.hrms.hrm.payroll.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "payroll_deduction")
public class PayrollDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String deductionType;
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;
}

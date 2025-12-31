package com.hrms.hrm.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payroll", uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "month", "year" }))
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID employeeId;

    private int month;
    private int year;

    @Column(name = "total_working_days", nullable = false)
    private int totalWorkingDays;

    @Column(name = "working_days", nullable = false)
    private int workingDays;

    private int presentDays;

    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;

    private BigDecimal netSalary;

    @CreationTimestamp
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;

    @Column(name = "paid_leave_days", nullable = false)
    private int paidLeaveDays;

    @Column(name = "unpaid_leave_days", nullable = false)
    private int unpaidLeaveDays;

    private LocalDateTime approvedAt;
    private UUID approvedBy;

    public enum PayrollStatus {
        GENERATED,
        PENDING_APPROVAL,
        APPROVED,
        PAID
    }

}

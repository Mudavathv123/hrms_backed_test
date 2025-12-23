package com.hrms.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private int month;
    private int year;

    // Salary Structure
    private Double basicSalary;
    private Double perDaySalary;

    // Attendance
    private int totalWorkingDays;
    private int presentDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;

    // Overtime
    private Long overtimeMinutes;
    private Double overtimeAmount;

    // Deductions
    private Double pfAmount;
    private Double professionalTax;
    private Double unpaidLeaveDeduction;
    private Double totalDeductions;

    // Final Salary
    private Double grossSalary;
    private Double netSalary;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status; // GENERATED, LOCKED

    private LocalDate generatedOn;

    public enum PayrollStatus {
        GENERATED,
        LOCKED
    }
}

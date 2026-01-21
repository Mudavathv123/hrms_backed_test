package com.hrms.hrm.payroll.model;

import com.hrms.hrm.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(
    name = "payroll",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* ===== Employee & Period ===== */

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    private int month;
    private int year;

    @Column(name = "pay_period_start")
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end")
    private LocalDate payPeriodEnd;

    /* ===== Attendance Summary ===== */

    @Column(name = "total_working_days", nullable = false)
    private int totalWorkingDays;

    @Column(name = "working_days", nullable = false)
    private int workingDays;

    @Column(name = "present_days", nullable = false)
    private int presentDays;

    @Column(name = "paid_leave_days", nullable = false)
    private int paidLeaveDays;

    @Column(name = "unpaid_leave_days", nullable = false)
    private int unpaidLeaveDays;

    @Column(name = "paid_days", nullable = false)
    private int paidDays;   

    /* ===== Salary Snapshot (for historical accuracy) ===== */

    @Column(name = "basic_salary", precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "hra", precision = 12, scale = 2)
    private BigDecimal hra;

    @Column(name = "allowance", precision = 12, scale = 2)
    private BigDecimal allowance;

    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "lop_amount", precision = 12, scale = 2)
    private BigDecimal lopAmount;

    @Column(name = "arrears", precision = 12, scale = 2)
    private BigDecimal arrears;

    @Column(name = "net_salary", precision = 12, scale = 2)
    private BigDecimal netSalary;

    /* ===== Statutory & Bank Snapshot ===== */

    @Column(name = "pf_number")
    private String pfNumber;

    @Column(name = "uan_number")
    private String uanNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    /* ===== Approval & Status ===== */

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;

    @CreationTimestamp
    private LocalDateTime generatedAt;

    private LocalDateTime approvedAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime paidAt;

    @Column(length = 500)
    private String remarks;

    public enum PayrollStatus {
        GENERATED,
        PENDING_APPROVAL,
        APPROVED,
        PAID
    }
}

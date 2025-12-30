package com.hrms.hrm.payroll.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payslip")
public class PaySlip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String pdfUrl;

    @OneToOne
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;

    @CreationTimestamp
    private LocalDateTime generatedAt;
}

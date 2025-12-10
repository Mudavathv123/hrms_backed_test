package com.hrms.hrm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "eod_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EodReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String employeeName;

    private String employeeCode;

    private String employeeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 1500)
    private String workSummary;

    private String blockers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDING,
        SUBMITTED,
        APPROVED,
        REJECTED
    }
}

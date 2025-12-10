package com.hrms.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // INFO / WARNING / ALERT

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "`read`",nullable = false)
    private boolean read;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_employee_id")
    private Employee sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_employee_id")
    private Employee receiver;

    @Enumerated(EnumType.STRING)
    private TargetRole targetRole;

    public enum TargetRole {
        HR, MANAGER, ADMIN, EMPLOYEE
    }

    public enum NotificationType {
        INFO,
        WARNING,
        ALERT,
        TASK
    }
}

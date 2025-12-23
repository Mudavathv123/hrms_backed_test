package com.hrms.hrm.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_receiver_read", columnList = "receiver_id, is_read"),
        @Index(name = "idx_notifications_receiver", columnList = "receiver_id"),
        @Index(name = "idx_notifications_date", columnList = "created_date")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = true)
    private LocalDate date;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    @JsonIgnore
    private Employee sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true)
    @JsonBackReference
    private Employee receiver;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private TargetRole targetRole;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;


    public enum NotificationType { 
        INFO,
        WARNING,
        ALERT,
        TASK
    }


    public enum TargetRole { 
        ROLE_ADMIN,
        ROLE_MANAGER,
        ROLE_EMPLOYEE
    }
}

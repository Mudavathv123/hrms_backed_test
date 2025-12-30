    package com.hrms.hrm.model;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDate;
    import java.util.UUID;

    @Entity
    @Table(name = "leaves")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class Leave {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id", nullable = false)
        private Employee employee;

        @Column(nullable = false)
        private LocalDate startDate;

        @Column(nullable = false)
        private LocalDate endDate;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private LeaveType leaveType;

        @Column(length = 500)
        private String reason;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private LeaveStatus status;

        @Column(length = 500)
        private String managerComment;

        @Column(nullable = false)
        private LocalDate appliedOn;

        private LocalDate actionOn;

        @Column(nullable = false)
        private int days;

        public enum LeaveType {
            SICK,
            CASUAL,
            ANNUAL,
            UNPAID,
            MATERNITY,
            PATERNITY,
            OTHER
        }

        public enum LeaveStatus {
            PENDING,
            APPROVED,
            REJECTED,
            CANCELLED
        }

        @PrePersist
        @PreUpdate
        public void calculateDays() {
            if(startDate != null && endDate !=null) {
                this.days = (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
            }
        }
    }

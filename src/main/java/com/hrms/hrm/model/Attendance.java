package com.hrms.hrm.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employee_id", "date" })
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference
    private Employee employee;

    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode")
    @Builder.Default
    private WorkMode workMode = WorkMode.OFFICE;

    @Column(name = "hours_worked")
    private Long hoursWorked;

    @Column(name = "minutes_worked")
    private Long minutesWorked;

    @Column(name = "work_time")
    private String workedTime;

    @Column(name = "total_minutes")
    private Long totalMinutes;

    @Column(name = "breaks_minutes")
    private Long breakMinutes;

    @Column(name = "overtime_minutes")
    private Long overtimeMinutes;

    private Boolean lateLogin;

    private Double latitude;
    private Double longitude;
    private String locationName;
    private Boolean isValidLocation;

    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        HALF_DAY,
        LATE,
        ON_LEAVE,
        WEEKEND,
        HOLIDAY
    }

    public enum WorkMode {
        OFFICE,
        WFH
    }

}

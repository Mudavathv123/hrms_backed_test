package com.hrms.hrm.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity

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

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private  AttendanceStatus status;

    @Column(name = "hours_worked")
    private Long hoursWorked;

    @Column(name = "minutes_worked")
    private Long minutesWorked;

    @Column(name = "work_time")
    private String workedTime;

    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        HALF_DAY,
        LATE,
        ON_LEAVE
    }


}

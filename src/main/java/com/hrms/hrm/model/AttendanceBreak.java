package com.hrms.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceBreak {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Attendance attendance;

    @Column(name = "break_start")
    private Instant breakStart;

    @Column(name = "break_end")
    private Instant breakEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "break_type")
    private BreakType type;

    public enum BreakType {
        LUNCH, TEA
    }
}


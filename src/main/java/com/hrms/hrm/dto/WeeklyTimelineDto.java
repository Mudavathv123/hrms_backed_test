package com.hrms.hrm.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyTimelineDto {

    private  UUID employeeId;
    private String firstName;
    private String lastName;
    private String employeeCode;
    private LocalDate date;
    private String checkInTime;
    private String checkOutTime;
    private String attendanceStatus;
    private Integer totalMinutes;
    private Integer overtimeMinutes;
    private boolean isLate;
    private String workMode; // WFH / OFFICE

}

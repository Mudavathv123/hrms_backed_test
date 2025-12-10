package com.hrms.hrm.dto;

import com.hrms.hrm.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponseDto {

    private UUID id;
    private UUID employeeId;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String attendanceStatus;
    private String workedTime;
    private String firstName;
    private String lastName;
    private String employeeCode;
}

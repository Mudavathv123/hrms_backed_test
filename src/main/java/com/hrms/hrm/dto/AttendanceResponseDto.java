package com.hrms.hrm.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponseDto {

    private UUID id;
    private UUID employeeId;
    private LocalDate date;
    private String checkInTime;
    private String checkOutTime;
    private String attendanceStatus;
    private String workedTime;
    private String firstName;
    private String lastName;
    private String employeeCode;
    private String workMode;

}

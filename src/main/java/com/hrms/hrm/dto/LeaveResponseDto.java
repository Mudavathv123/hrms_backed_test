package com.hrms.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveResponseDto {

    private UUID id;
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private int days;
    private String status;
    private String reason;
    private String managerComment;
    private LocalDate appliedOn;
    private LocalDate actionOn;
}

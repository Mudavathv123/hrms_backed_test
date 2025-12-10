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
public class TaskResponseDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority;
    private String status;
    private UUID assignedToEmployeeId;
    private String employeeCode;
    private String assignedToEmployeeName;
    private LocalDate startDate;

}


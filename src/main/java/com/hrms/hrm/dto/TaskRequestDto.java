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
public class TaskRequestDto {

    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority; // LOW / MEDIUM / HIGH
    private String assignedToEmployeeId;
    private LocalDate startDate;

}

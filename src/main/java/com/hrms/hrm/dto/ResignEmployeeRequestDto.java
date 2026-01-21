package com.hrms.hrm.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class ResignEmployeeRequestDto {
    private UUID employeeId;
    private LocalDate resignationDate;
    private String reason;
}

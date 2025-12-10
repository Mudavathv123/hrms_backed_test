package com.hrms.hrm.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EodResponseDto {

    private UUID id;
    private String employeeName;
    private String employeeCode;
    private String employeeId;
    private LocalDate date;
    private String workSummary;
    private String blockers;
    private String status;
}

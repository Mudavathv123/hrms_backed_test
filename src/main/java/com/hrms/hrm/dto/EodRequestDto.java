package com.hrms.hrm.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EodRequestDto {

    private String employeeCode;
    private LocalDate date;
    private String workSummary;
    private String blockers;
    private String employeeId;
    private String status;  // PENDING / SUBMITTED / APPROVED / REJECTED
}

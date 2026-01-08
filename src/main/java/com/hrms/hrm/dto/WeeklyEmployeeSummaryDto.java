package com.hrms.hrm.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyEmployeeSummaryDto {
    private UUID employeeId;
    private String employeeName;
    private WeeklySummaryDto summary;
}
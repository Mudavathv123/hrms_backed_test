package com.hrms.hrm.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyAdminResponseDto {
    private List<WeeklyEmployeeSummaryDto> employees;
    private WeeklySummaryDto overallSummary;
}

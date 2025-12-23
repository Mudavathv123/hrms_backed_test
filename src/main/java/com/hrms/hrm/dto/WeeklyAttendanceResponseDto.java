package com.hrms.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyAttendanceResponseDto {

    private List<AttendanceResponseDto> records;

    private WeeklySummaryDto summary;
}

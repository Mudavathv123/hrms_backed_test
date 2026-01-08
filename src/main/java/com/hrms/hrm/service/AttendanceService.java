package com.hrms.hrm.service;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.MonthlySummaryDto;
import com.hrms.hrm.dto.WeeklyAdminResponseDto;
import com.hrms.hrm.dto.WeeklySummaryDto;
import com.hrms.hrm.dto.WeeklyTimelineDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

public interface AttendanceService {

    AttendanceResponseDto checkIn(UUID employeeId, Double latitude, Double longitude, String locationName);

    AttendanceResponseDto checkOut(UUID employeeId);

    AttendanceResponseDto getTodayAttendanceByEmployee(UUID employeeId);

    List<AttendanceResponseDto> getAttendanceHistory(UUID employeeId);

    MonthlySummaryDto getMonthlyAttendance(UUID employeeId, int year, int month);

    List<AttendanceResponseDto> getTodayAttendance();

    List<AttendanceResponseDto> getAttendanceByDate(String date);

    WeeklyAdminResponseDto getWeeklyAttendance(UUID employeeId, LocalDate weekStart);

    void autoCheckoutEndOfDay();

    List<WeeklyTimelineDto> getWeeklyTimeline(
            Authentication auth,
            UUID employeeId,
            LocalDate weekStart);

    WeeklyAdminResponseDto getAllWeeklySummary(LocalDate weekStart);

    List<WeeklyTimelineDto> getMyTimeline(Authentication auth, LocalDate weekStart);

    List<WeeklyTimelineDto> getAllTimelines(LocalDate weekStart, UUID employeeId);

    WeeklySummaryDto getMyWeeklySummary(Authentication auth, LocalDate weekStart);

}

package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.MonthlySummaryDto;
import com.hrms.hrm.dto.WeeklyAdminResponseDto;
import com.hrms.hrm.dto.WeeklySummaryDto;
import com.hrms.hrm.dto.WeeklyTimelineDto;
import com.hrms.hrm.service.AttendanceService;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> checkIn(@PathVariable UUID employeeId, 
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String locationName) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkIn(employeeId, latitude, longitude, locationName), "check in successful"));
    }

    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> checkOut(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkOut(employeeId), "check out successful"));
    }

    @GetMapping("/today/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> getTodayAttendanceByEmployee(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayAttendanceByEmployee(employeeId),
                "fetched today employee attendance success"));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getTodayAttendance() {
        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.getTodayAttendance(), "fetched today attendance success"));
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceHistory(employeeId),
                "attendance history fetched successful"));
    }

    @GetMapping("/monthly/{employeeId}")
    public ResponseEntity<ApiResponse<?>> getMonthlyAttendance(@PathVariable UUID employeeId,
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMonthlyAttendance(employeeId, year, month),
                "monthly attendance fetched successful.."));

    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getAttendanceByDate(@RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByDate(date),
                "selected date attendance fetched success"));
    }

    @GetMapping("/weekly/timeline")
    public ResponseEntity<ApiResponse<List<WeeklyTimelineDto>>> getWeeklyTimeline(
            @RequestParam LocalDate weekStart,
            @RequestParam(required = false) UUID employeeId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        attendanceService.getWeeklyTimeline(authentication, employeeId, weekStart),
                        "Weekly timeline fetched"));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyAdminResponseDto>> getWeeklyAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        attendanceService.getWeeklyAttendance(null, weekStart),
                        "weekly attendance fetched"));
    }

    @GetMapping("/weekly/{employeeId}")
    public ResponseEntity<?> getWeeklyAttendanceByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(
                attendanceService.getWeeklyAttendance(employeeId, weekStart));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlySummaryDto>> getMonthlyAttendance(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID employeeId) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        attendanceService.getMonthlyAttendance(employeeId, year, month),
                        "monthly attendance fetched"));
    }

    @GetMapping("/monthly/employee/{employeeId}")
    public ResponseEntity<?> getMonthlyAttendanceByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(
                attendanceService.getMonthlyAttendance(employeeId, year, month));
    }

    @GetMapping("/summary/me")
    public ApiResponse<WeeklySummaryDto> myWeeklySummary(
            @RequestParam LocalDate weekStart,
            Authentication auth) {

        return ApiResponse.success(
                attendanceService.getMyWeeklySummary(auth, weekStart),
                "My weekly summary");
    }

    @GetMapping("/summary/all")
    public ApiResponse<WeeklyAdminResponseDto> allWeeklySummary(
            @RequestParam LocalDate weekStart) {

        return ApiResponse.success(
                attendanceService.getAllWeeklySummary(weekStart),
                "All employees weekly summary");
    }

    @GetMapping("/timeline/me")
    public ApiResponse<List<WeeklyTimelineDto>> myTimeline(
            @RequestParam LocalDate weekStart,
            Authentication auth) {

        return ApiResponse.success(
                attendanceService.getMyTimeline(auth, weekStart),
                "My weekly timeline");
    }

    @GetMapping("/timeline/all")
    public ApiResponse<List<WeeklyTimelineDto>> allTimeline(
            @RequestParam LocalDate weekStart,
            @RequestParam(required = false) UUID employeeId) {

        return ApiResponse.success(
                attendanceService.getAllTimelines(weekStart, employeeId),
                "All weekly timelines");
    }

}

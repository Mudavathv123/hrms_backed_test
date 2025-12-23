package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.WeeklyAttendanceResponseDto;
import com.hrms.hrm.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.hibernate.loader.ast.spi.Loadable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> checkIn(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkIn(employeeId), "check in successful"));
    }

    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> checkOut(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.checkOut(employeeId), "check out successful"));
    }

    @GetMapping("/today/{employeeId}")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> getTodayAttendanceByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayAttendanceByEmployee(employeeId), "fetched today employee attendance success"));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getTodayAttendance() {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayAttendance(), "fetched today attendance success"));
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceHistory(employeeId),"attendance history fetched successful"));
    }

    @GetMapping("/monthly/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getMonthlyAttendance(@PathVariable UUID employeeId,
                                                                                         @RequestParam int year,@RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMonthlyAttendance(employeeId, year, month), "monthly attendance fetched successful.."));

    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>>  getAttendanceByDate(@RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAttendanceByDate(date), "selected date attendance fetched success"));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyAttendanceResponseDto>> getWeeklyAttendance(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam String weekStart
    ) {
        LocalDate startDate = LocalDate.parse(weekStart);
        return ResponseEntity.ok(
                ApiResponse.success(
                        attendanceService.getWeeklyAttendance(employeeId, startDate)
                        , "weekly attendance fetched successfully"
                )
        );
    }

}

    package com.hrms.hrm.service;

    import com.hrms.hrm.dto.AttendanceResponseDto;
    import com.hrms.hrm.dto.WeeklyAttendanceResponseDto;

    import java.time.LocalDate;
    import java.util.List;
    import java.util.UUID;

    public interface AttendanceService {

        AttendanceResponseDto checkIn(UUID employeeId);

        AttendanceResponseDto checkOut(UUID employeeId);

        AttendanceResponseDto getTodayAttendanceByEmployee(UUID employeeId);

        List<AttendanceResponseDto> getAttendanceHistory(UUID employeeId);

        List<AttendanceResponseDto> getMonthlyAttendance(UUID employeeId, int year, int month);

        List<AttendanceResponseDto> getTodayAttendance();

        List<AttendanceResponseDto> getAttendanceByDate(String date);

        WeeklyAttendanceResponseDto getWeeklyAttendance(UUID employeeId, LocalDate weekStart);

        void autoCheckoutEndOfDay();


    }

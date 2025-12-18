package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.service.AttendanceService;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    @Override
    public AttendanceResponseDto checkIn(UUID employeeId) {
        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .ifPresent(ex -> {
                    throw new RuntimeException("Employee already checked in today");
                });

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .checkInTime(LocalDateTime.now())
                .date(today)
                .status(Attendance.AttendanceStatus.PRESENT)
                .workedTime("00:00")
                .hoursWorked(null)
                .minutesWorked(null)
                .build();

        attendance = attendanceRepository.save(attendance);


        try {
            notificationService.sendNotification(
                    NotificationRequestDto.builder()
                            .type("ATTENDANCE")
                            .date(today)
                            .title("Check-In Recorded")
                            .message("Employee " + employee.getFirstName() + " " + employee.getLastName() + " has checked in today.")
                            .senderId(employee.getId())
                            .receiverId(null) // Broadcast to Admins
                            .targetRole("ROLE_ADMIN")
                            .build()
            );
            log.info("Notification sent for employee check-in: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send check-in notification for employee {}: {}", employee.getEmail(), e.getMessage(), e);
        }

        return DtoMapper.toDto(attendance);
    }

    @Override
    public AttendanceResponseDto checkOut(UUID employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("Employee has not checked in today"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Employee already checked out today");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        if (attendance.getCheckInTime() != null) {
            calculateAttendance(attendance);
        }

        attendance = attendanceRepository.save(attendance);

        try {
            Employee employee = attendance.getEmployee();
            notificationService.sendNotification(
                    NotificationRequestDto.builder()
                            .type("ATTENDANCE")
                            .date(today)
                            .title("Check-Out Recorded")
                            .message("Employee " + employee.getFirstName() + " " + employee.getLastName() + " has checked out today.")
                            .senderId(employee.getId())
                            .receiverId(null) // Broadcast to Admins
                            .targetRole("ROLE_ADMIN")
                            .build()
            );
            log.info("Notification sent for employee check-out: {}", attendance.getEmployee().getEmail());
        } catch (Exception e) {
            log.error("Failed to send check-out notification for employee {}: {}", attendance.getEmployee().getEmail(), e.getMessage(), e);
        }

        return DtoMapper.toDto(attendance);
    }

    @Override
    public AttendanceResponseDto getTodayAttendanceByEmployee(UUID employeeId) {
        return attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .map(DtoMapper::toDto)
                .orElse(null);
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceHistory(UUID employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<AttendanceResponseDto> getMonthlyAttendance(UUID employeeId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<AttendanceResponseDto> getTodayAttendance() {
        LocalDate date = LocalDate.now();
        return attendanceRepository.findByDate(date)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByDate(String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
        return attendanceRepository.findByDate(parsedDate)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    private void calculateAttendance(Attendance attendance) {
        LocalDateTime checkIn = attendance.getCheckInTime();
        LocalDateTime checkOut = attendance.getCheckOutTime();

        if (checkIn != null && checkOut != null) {
            long totalMinutes = Duration.between(checkIn, checkOut).toMinutes();
            long hoursWorked = totalMinutes / 60;
            long minutesWorked = totalMinutes % 60;

            attendance.setHoursWorked(hoursWorked);
            attendance.setMinutesWorked(minutesWorked);
            attendance.setWorkedTime(String.format("%02d:%02d", hoursWorked, minutesWorked));

            if (totalMinutes < 240) {
                attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
            } else {
                attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
            }
        } else {
            attendance.setHoursWorked(null);
            attendance.setMinutesWorked(null);
            attendance.setWorkedTime("00:00");
            attendance.setStatus(Attendance.AttendanceStatus.ABSENT);
        }
    }
}

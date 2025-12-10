package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AttendanceServiceImpl implements com.hrms.hrm.service.AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public AttendanceResponseDto checkIn(UUID employeeId) {
        LocalDate today = LocalDate.now();

        attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .ifPresent(ex -> {
                    throw new RuntimeException("Employee already checked today");
                });

        Attendance attendance = Attendance.builder()
                .employee(employeeRepository.findById(employeeId).orElseThrow(() ->  new ResourceNotFoundException("Employee not found!!")))
                .checkInTime(LocalDateTime.now())
                .date(today)
                .build();

        attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        attendance.setHoursWorked(null);

        attendance = attendanceRepository.save(attendance);
        return DtoMapper.toDto(attendance);
    }

    @Override
    public AttendanceResponseDto checkOut(UUID employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("Employee has not checked in today"));

        if(attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Employee already check out today");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        if(attendance.getCheckInTime() != null) {
           calculateAttendance(attendance);
        }
        attendance = attendanceRepository.save(attendance);
        return DtoMapper.toDto(attendance);
    }

    @Override
    public AttendanceResponseDto getTodayAttendanceByEmployee(UUID employeeId) {
        return attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .map(DtoMapper::toDto).orElse(null);
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceHistory(UUID employeeId) {
        return attendanceRepository
                .findByEmployeeId(employeeId)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<AttendanceResponseDto> getMonthlyAttendance(UUID employeeId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, start, end)
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

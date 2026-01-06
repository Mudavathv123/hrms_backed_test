package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.WeeklyAttendanceResponseDto;
import com.hrms.hrm.dto.WeeklySummaryDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.AttendanceBreakRepository;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.AttendanceService;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

        private final AttendanceRepository attendanceRepository;
        private final EmployeeRepository employeeRepository;
        private final NotificationService notificationService;
        private final AttendanceBreakRepository breakRepository;
        private final UserRepository userRepository;

        // ================= CHECK IN =================

        @Override
        public AttendanceResponseDto checkIn(UUID employeeId) {

            LocalDate today = LocalDate.now();

            if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, today)) {
                throw new RuntimeException("Already checked in today");
            }

            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

            Attendance attendance = Attendance.builder()
                    .employee(employee)
                    .date(today)
                    .checkInTime(LocalDateTime.now())
                    .lateLogin(isLateLogin())
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .build();

            attendanceRepository.save(attendance);
            sendNotification(employee, "Check-In Recorded");

            return DtoMapper.toDto(attendance);
        }

        // ================= CHECK OUT =================

        @Override
        public AttendanceResponseDto checkOut(UUID employeeId) {

            Attendance attendance = getTodayAttendance(employeeId);

            if (attendance.getCheckOutTime() != null) {
                throw new RuntimeException("Already checked out");
            }

            attendance.setCheckOutTime(LocalDateTime.now());
            calculateFinalAttendance(attendance);

            attendanceRepository.save(attendance);
            sendNotification(attendance.getEmployee(), "Check-Out Recorded");

            return DtoMapper.toDto(attendance);
        }

        // ================= CALCULATION =================

        private void calculateFinalAttendance(Attendance attendance) {

            long workedMinutes = Duration
                    .between(attendance.getCheckInTime(), attendance.getCheckOutTime())
                    .toMinutes();

            long breakMinutes = breakRepository
                    .findByAttendance(attendance)
                    .stream()
                    .mapToLong(b ->
                            Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes())
                    .sum();

            long netMinutes = workedMinutes - breakMinutes;

            attendance.setTotalMinutes(netMinutes);
            attendance.setBreakMinutes(breakMinutes);
            attendance.setOvertimeMinutes(Math.max(0, netMinutes - 480));
            attendance.setWorkedTime(format(netMinutes));

            if (netMinutes < 240) {
                attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
            } else {
                attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
            }
        }

        private String format(long minutes) {
            return String.format("%02d:%02d", minutes / 60, minutes % 60);
        }

        private boolean isLateLogin() {
            return LocalTime.now().isAfter(LocalTime.of(9, 30));
        }

        // ================= HELPERS =================

        private Attendance getTodayAttendance(UUID employeeId) {
            return attendanceRepository
                    .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                    .orElseThrow(() -> new RuntimeException("Not checked in"));
        }

        private void sendNotification(Employee emp, String title) {
            try {
                notificationService.sendNotification(
                        NotificationRequestDto.builder()
                                .type("ATTENDANCE")
                                .date(LocalDate.now())
                                .title(title)
                                .message("Employee " + emp.getFirstName() + " " + emp.getLastName())
                                .senderId(emp.getId())
                                .targetRole("ROLE_ADMIN")
                                .build()
                );
            } catch (Exception e) {
                log.error("Notification failed", e);
            }
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

    @Override
    public WeeklyAttendanceResponseDto getWeeklyAttendance(UUID employeeId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = currentUser.getRole().name();


        boolean isAdminOrHr = role.equals("ROLE_ADMIN") || role.equals("ROLE_HR");

        if (!isAdminOrHr) {
            List<Attendance> records = attendanceRepository.findWeeklyAttendance(employeeId, weekStart, weekEnd);
            return buildWeeklyResponse(employeeId, records, weekStart);
        } else {

            List<Employee> employees = employeeRepository.findAll();
            List<AttendanceResponseDto> allRecords = new ArrayList<>();
            WeeklySummaryDto summary = new WeeklySummaryDto();

            for (Employee emp : employees) {
                List<Attendance> empRecords = attendanceRepository.findWeeklyAttendance(emp.getId(), weekStart, weekEnd);
                WeeklyAttendanceResponseDto empWeekly = buildWeeklyResponse(emp.getId(), empRecords, weekStart);
                allRecords.addAll(empWeekly.getRecords());

                summary.setPresent(summary.getPresent() + empWeekly.getSummary().getPresent());
                summary.setHalfDay(summary.getHalfDay() + empWeekly.getSummary().getHalfDay());
                summary.setLeave(summary.getLeave() + empWeekly.getSummary().getLeave());
                summary.setWeekend(summary.getWeekend() + empWeekly.getSummary().getWeekend());
                summary.setPayableDays(summary.getPayableDays() + empWeekly.getSummary().getPayableDays());
            }

            return new WeeklyAttendanceResponseDto(allRecords, summary);
        }
    }

    private WeeklyAttendanceResponseDto buildWeeklyResponse(UUID employeeId, List<Attendance> records, LocalDate weekStart) {
        Map<LocalDate, Attendance> attendanceMap = new HashMap<>();
        for (Attendance a : records) attendanceMap.put(a.getDate(), a);

        List<AttendanceResponseDto> result = new ArrayList<>();
        int present = 0, halfDay = 0, leave = 0, weekend = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            Attendance attendance = attendanceMap.get(date);

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) weekend++;

            if (attendance == null) {
                result.add(AttendanceResponseDto.builder()
                        .employeeId(employeeId)
                        .date(date)
                        .attendanceStatus(String.valueOf(Attendance.AttendanceStatus.ABSENT))
                        .build()
                );
                leave++;
            } else {
                result.add(DtoMapper.toDto(attendance));
                switch (attendance.getStatus()) {
                    case PRESENT -> present++;
                    case HALF_DAY -> halfDay++;
                    case ON_LEAVE -> leave++;
                }
            }
        }

        WeeklySummaryDto summary = new WeeklySummaryDto(present, halfDay, leave, weekend, present + halfDay + leave);
        return new WeeklyAttendanceResponseDto(result, summary);
    }
  

    @Override
    public void autoCheckoutEndOfDay() {
        LocalDate today = LocalDate.now();

        List<Attendance> records = attendanceRepository.findByDateAndCheckedInWithoutCheckout(today);

        records.forEach(rec -> {

            rec.setCheckOutTime(LocalDateTime.of(today, LocalTime.of(23, 59)));

            calculateFinalAttendance(rec);

            attendanceRepository.save(rec);
        });

        log.info("Auto checkout completed for {} records at end of day", records.size());
    }

}



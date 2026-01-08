package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.MonthlySummaryDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.WeeklyAdminResponseDto;

import com.hrms.hrm.dto.WeeklyEmployeeSummaryDto;
import com.hrms.hrm.dto.WeeklySummaryDto;
import com.hrms.hrm.dto.WeeklyTimelineDto;
import com.hrms.hrm.error.InvalidLocationException;
import com.hrms.hrm.error.LocationPermissionException;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.AllowedLocation;
import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.AllowedLocationRepository;
import com.hrms.hrm.repository.AttendanceBreakRepository;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.repository.WfhPolicyRepository;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final AttendanceBreakRepository breakRepository;
    private final UserRepository userRepository;
    private final WfhPolicyRepository wfhPolicyRepository;
    private final AllowedLocationRepository allowedLocationRepository;

    private boolean validateLocation(Double lat, Double lon) {
        List<AllowedLocation> allowedLocations = allowedLocationRepository.findAll();

        for (AllowedLocation loc : allowedLocations) {
            double distance = calculateDistance(lat, lon, loc.getLatitude(), loc.getLongitude());
            if (distance <= loc.getRadiusInMeters()) {
                return true;
            }
        }
        return false;
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ================= CHECK IN =================

    @Override
    public AttendanceResponseDto checkIn(
            UUID employeeId,
            Double latitude,
            Double longitude,
            String locationName) {

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, today)) {
            throw new RuntimeException("Already checked in today");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        boolean isWFH = wfhPolicyRepository.existsByIsGlobalTrueAndEnabledTrue()
                || wfhPolicyRepository.existsByEmployee_IdAndEnabledTrue(employeeId);
                
        boolean isValidLocation = validateLocation(latitude, longitude);

        if (!isWFH) {
            if (latitude == null || longitude == null) {
                throw new LocationPermissionException(
                        "Location permission required for office check-in");
            }

            if (!isValidLocation) {
                throw new InvalidLocationException(
                        "Outside allowed office location");
            }
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkInTime(LocalDateTime.now())
                .lateLogin(isLateLogin())
                .status(Attendance.AttendanceStatus.PRESENT)
                .latitude(isWFH ? null : latitude)
                .longitude(isWFH ? null : longitude)
                .locationName(isWFH ? "Work From Home" : locationName)
                .isValidLocation(isValidLocation)
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
                .mapToLong(b -> Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes())
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
                            .build());
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
    public MonthlySummaryDto getMonthlyAttendance(UUID employeeId, int year, int month) {

        User currentUser = getLoggedInUser();
        boolean isAdminOrHr = isAdminOrHr(currentUser);

        UUID empId = isAdminOrHr && employeeId != null
                ? employeeId
                : currentUser.getEmployee().getId();

        return calculateMonthlySummary(empId, year, month);
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
    public WeeklyAdminResponseDto getWeeklyAttendance(UUID employeeId, LocalDate weekStart) {

        LocalDate weekEnd = weekStart.plusDays(6);
        User currentUser = getLoggedInUser();
        boolean isAdminOrHr = isAdminOrHr(currentUser);

        if (!isAdminOrHr) {
            UUID empId = currentUser.getEmployee().getId();

            WeeklySummaryDto summary = calculateWeeklySummary(empId, weekStart, weekEnd);

            return WeeklyAdminResponseDto.builder()
                    .employees(List.of(
                            WeeklyEmployeeSummaryDto.builder()
                                    .employeeId(empId)
                                    .employeeName(
                                            currentUser.getEmployee().getFirstName() + " " +
                                                    currentUser.getEmployee().getLastName())
                                    .summary(summary)
                                    .build()))
                    .overallSummary(summary)
                    .build();
        }

        List<Employee> employees = employeeRepository.findAll();

        List<WeeklyEmployeeSummaryDto> employeeSummaries = new ArrayList<>();
        WeeklySummaryDto overall = new WeeklySummaryDto();

        for (Employee emp : employees) {
            WeeklySummaryDto summary = calculateWeeklySummary(emp.getId(), weekStart, weekEnd);

            employeeSummaries.add(
                    WeeklyEmployeeSummaryDto.builder()
                            .employeeId(emp.getId())
                            .employeeName(emp.getFirstName() + " " + emp.getLastName())
                            .summary(summary)
                            .build());

            overall.add(summary);
        }

        return WeeklyAdminResponseDto.builder()
                .employees(employeeSummaries)
                .overallSummary(overall)
                .build();
    }

    @Override
    public List<WeeklyTimelineDto> getWeeklyTimeline(
            Authentication auth,
            UUID employeeId,
            LocalDate weekStart) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = weekStart;
        LocalDate end = weekStart.plusDays(6);

        if (user.getRole() == User.Role.ROLE_EMPLOYEE) {

            if (user.getEmployee() == null) {
                throw new RuntimeException("Logged-in user has no employee assigned");
            }
            UUID empId = user.getEmployee().getId();
            List<Attendance> records = attendanceRepository.findWeeklyAttendance(empId, start, end);
            return records.stream()
                    .map(this::mapToTimelineDto)
                    .toList();

        } else {

            if (employeeId != null) {
                // Specific employee timeline
                List<Attendance> records = attendanceRepository.findWeeklyAttendance(employeeId, start, end);
                return records.stream()
                        .map(this::mapToTimelineDto)
                        .toList();
            } else {

                List<Employee> employees = employeeRepository.findAll();
                List<WeeklyTimelineDto> allTimelines = new ArrayList<>();
                for (Employee emp : employees) {
                    List<Attendance> records = attendanceRepository.findWeeklyAttendance(emp.getId(), start, end);
                    records.stream()
                            .map(this::mapToTimelineDto)
                            .forEach(allTimelines::add);
                }
                return allTimelines;
            }
        }
    }

    @Override
    public WeeklyAdminResponseDto getAllWeeklySummary(LocalDate weekStart) {

        List<Employee> employees = employeeRepository.findAll();
        WeeklySummaryDto overall = new WeeklySummaryDto();
        List<WeeklyEmployeeSummaryDto> list = new ArrayList<>();

        for (Employee emp : employees) {
            WeeklySummaryDto summary = calculateWeeklySummary(emp.getId(), weekStart, weekStart.plusDays(6));

            list.add(new WeeklyEmployeeSummaryDto(
                    emp.getId(),
                    emp.getFirstName() + " " + emp.getLastName(),
                    summary));

            overall.add(summary);
        }

        return new WeeklyAdminResponseDto(list, overall);
    }

    @Override
    public List<WeeklyTimelineDto> getMyTimeline(Authentication auth, LocalDate weekStart) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return attendanceRepository
                .findWeeklyAttendance(user.getEmployee().getId(),
                        weekStart, weekStart.plusDays(6))
                .stream()
                .map(this::mapToTimelineDto)
                .toList();
    }

    @Override
    public List<WeeklyTimelineDto> getAllTimelines(LocalDate weekStart, UUID employeeId) {

        LocalDate end = weekStart.plusDays(6);

        if (employeeId != null) {
            return attendanceRepository.findWeeklyAttendance(employeeId, weekStart, end)
                    .stream().map(this::mapToTimelineDto).toList();
        }

        return employeeRepository.findAll().stream()
                .flatMap(emp -> attendanceRepository.findWeeklyAttendance(emp.getId(), weekStart, end).stream())
                .map(this::mapToTimelineDto)
                .toList();
    }

    @Override
    public WeeklySummaryDto getMyWeeklySummary(Authentication auth, LocalDate weekStart) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID empId = user.getEmployee().getId();
        return calculateWeeklySummary(empId, weekStart, weekStart.plusDays(6));
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

    private WeeklySummaryDto calculateWeeklySummary(UUID employeeId, LocalDate start, LocalDate end) {

        List<Attendance> records = attendanceRepository.findWeeklyAttendance(employeeId, start, end);

        int present = 0, halfDay = 0, leave = 0, weekend = 0;

        Map<LocalDate, Attendance> map = records.stream().collect(Collectors.toMap(Attendance::getDate, a -> a));

        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekend++;
                continue;
            }

            Attendance a = map.get(date);
            if (a == null) {
                leave++;
            } else {
                switch (a.getStatus()) {
                    case PRESENT -> present++;
                    case HALF_DAY -> halfDay++;
                    case ON_LEAVE -> leave++;
                }
            }
        }

        return new WeeklySummaryDto(
                present,
                halfDay,
                leave,
                weekend,
                present + halfDay);
    }

    private MonthlySummaryDto calculateMonthlySummary(UUID employeeId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> records = attendanceRepository.findWeeklyAttendance(employeeId, start, end);

        int present = 0, halfDay = 0, leave = 0, weekend = 0;

        Map<LocalDate, Attendance> map = records.stream().collect(Collectors.toMap(Attendance::getDate, a -> a));

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekend++;
                continue;
            }

            Attendance a = map.get(date);
            if (a == null) {
                leave++;
            } else {
                switch (a.getStatus()) {
                    case PRESENT -> present++;
                    case HALF_DAY -> halfDay++;
                    case ON_LEAVE -> leave++;
                }
            }
        }

        return new MonthlySummaryDto(
                present,
                halfDay,
                leave,
                weekend,
                present + halfDay);
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdminOrHr(User user) {
        return user.getRole() == User.Role.ROLE_ADMIN ||
                user.getRole() == User.Role.ROLE_HR;
    }

    private WeeklyTimelineDto mapToTimelineDto(Attendance a) {

        Employee emp = a.getEmployee();

        return new WeeklyTimelineDto(
                emp.getId(),
                emp.getFirstName(),
                emp.getLastName(),
                emp.getEmployeeId(),

                a.getDate(),

                a.getCheckInTime() != null
                        ? a.getCheckInTime().toLocalTime()
                        : null,

                a.getCheckOutTime() != null
                        ? a.getCheckOutTime().toLocalTime()
                        : null,

                a.getStatus() != null
                        ? a.getStatus().name()
                        : "UNKNOWN",

                a.getTotalMinutes() != null
                        ? a.getTotalMinutes().intValue()
                        : 0,

                a.getOvertimeMinutes() != null
                        ? a.getOvertimeMinutes().intValue()
                        : 0);
    }

}

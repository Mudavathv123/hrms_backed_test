package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.AttendanceResponseDto;
import com.hrms.hrm.dto.MonthlySummaryDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.WeeklyAdminResponseDto;

import com.hrms.hrm.dto.WeeklyEmployeeSummaryDto;
import com.hrms.hrm.dto.WeeklySummaryDto;
import com.hrms.hrm.dto.WeeklyTimelineDto;
import com.hrms.hrm.error.AlreadyCheckedInException;
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
import com.hrms.hrm.repository.HolidayRepository;
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
    private final HolidayRepository holidayRepository;
    private final AllowedLocationRepository allowedLocationRepository;

    private static final LocalTime OFFICE_START_TIME = LocalTime.of(9, 30);

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
    public AttendanceResponseDto checkIn(
            UUID employeeId,
            Double latitude,
            Double longitude,
            String locationName) {

        LocalDate today = LocalDate.now();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Attendance attendance = attendanceRepository
                .findFirstByEmployeeIdAndDate(employeeId, today)
                .orElse(null);

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setDate(today);
        }

        if (attendance.getCheckInTime() != null) {
            throw new AlreadyCheckedInException("Already checked in today");
        }

        boolean isWFH = wfhPolicyRepository.existsByIsGlobalTrueAndEnabledTrue()
                || wfhPolicyRepository.existsByEmployee_IdAndEnabledTrue(employeeId);

        attendance.setWorkMode(isWFH
                ? Attendance.WorkMode.WFH
                : Attendance.WorkMode.OFFICE);

        if (!isWFH) {
            if (latitude == null || longitude == null) {
                throw new LocationPermissionException(
                        "Location permission required for office check-in");
            }

            if (!validateLocation(latitude, longitude)) {
                throw new InvalidLocationException(
                        "Outside allowed office location");
            }
        }

        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setLatitude(latitude);
        attendance.setLongitude(longitude);
        attendance.setLocationName(locationName);

        attendanceRepository.save(attendance);

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

    private boolean isLate(Attendance attendance) {

        if (attendance.getCheckInTime() == null)
            return false;

        return attendance.getCheckInTime()
                .toLocalTime()
                .isAfter(OFFICE_START_TIME);
    }

    // ================= HELPERS =================

    private Attendance getTodayAttendance(UUID employeeId) {
        return attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Not checked in"));
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

        LocalDate today = LocalDate.now();

        return attendanceRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .map(DtoMapper::toDto)
                .orElseGet(() -> {

                    boolean isHoliday = holidayRepository.existsByDate(today);
                    DayOfWeek day = today.getDayOfWeek();

                    String status;
                    if (isHoliday) {
                        status = Attendance.AttendanceStatus.HOLIDAY.name();
                    } else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                        status = Attendance.AttendanceStatus.WEEKEND.name();
                    } else {
                        status = Attendance.AttendanceStatus.ABSENT.name();
                    }

                    Employee emp = employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

                    return AttendanceResponseDto.builder()
                            .employeeId(emp.getId())
                            .firstName(emp.getFirstName())
                            .lastName(emp.getLastName())
                            .employeeCode(emp.getEmployeeId())
                            .date(today)
                            .attendanceStatus(status)
                            .workedTime("00:00")
                            .build();
                });
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
    public MonthlySummaryDto getAllMonthlyAttendance(int year, int month) {

        List<Employee> employees = employeeRepository.findAll();

        MonthlySummaryDto overall = new MonthlySummaryDto(0, 0, 0, 0, 0);

        for (Employee emp : employees) {
            MonthlySummaryDto summary = calculateMonthlySummary(emp.getId(), year, month);

            overall.add(summary);
        }

        return overall;
    }

    @Override
    public List<AttendanceResponseDto> getTodayAttendance() {

        LocalDate today = LocalDate.now();

        List<Employee> employees = employeeRepository.findAll();

        Map<UUID, Attendance> attendanceMap = attendanceRepository
                .findByDate(today)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getEmployee().getId(),
                        a -> a,
                        (a1, a2) -> a1));

        List<AttendanceResponseDto> result = new ArrayList<>();

        for (Employee emp : employees) {

            Attendance attendance = attendanceMap.get(emp.getId());

            if (attendance != null) {
                result.add(DtoMapper.toDto(attendance));
            } else {

                boolean isHoliday = holidayRepository.existsByDate(today);
                DayOfWeek day = today.getDayOfWeek();

                String status;
                if (isHoliday) {
                    status = Attendance.AttendanceStatus.HOLIDAY.name();
                } else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    status = Attendance.AttendanceStatus.WEEKEND.name();
                } else {
                    status = Attendance.AttendanceStatus.ABSENT.name();
                }

                result.add(AttendanceResponseDto.builder()
                        .employeeId(emp.getId())
                        .firstName(emp.getFirstName())
                        .lastName(emp.getLastName())
                        .employeeCode(emp.getEmployeeId())
                        .date(today)
                        .attendanceStatus(status)
                        .workedTime("00:00")
                        .build());
            }

        }

        return result;
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByDate(String date) {

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }

        List<Employee> employees = employeeRepository.findAll();

        Map<UUID, Attendance> attendanceMap = attendanceRepository
                .findByDate(parsedDate)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getEmployee().getId(),
                        a -> a,
                        (a1, a2) -> a1));

        boolean isHoliday = holidayRepository.existsByDate(parsedDate);
        DayOfWeek day = parsedDate.getDayOfWeek();

        List<AttendanceResponseDto> result = new ArrayList<>();

        for (Employee emp : employees) {

            Attendance attendance = attendanceMap.get(emp.getId());

            if (attendance != null) {
                result.add(DtoMapper.toDto(attendance));
            } else {

                String status;
                if (isHoliday) {
                    status = Attendance.AttendanceStatus.HOLIDAY.name();
                } else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    status = Attendance.AttendanceStatus.WEEKEND.name();
                } else {
                    status = Attendance.AttendanceStatus.ABSENT.name();
                }

                result.add(AttendanceResponseDto.builder()
                        .employeeId(emp.getId())
                        .firstName(emp.getFirstName())
                        .lastName(emp.getLastName())
                        .employeeCode(emp.getEmployeeId())
                        .date(parsedDate)
                        .attendanceStatus(status)
                        .workedTime("00:00")
                        .build());
            }
        }

        return result;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate start = weekStart;
        LocalDate end = weekStart.plusDays(6);

        List<Employee> employees;

        if (user.getRole() == User.Role.ROLE_EMPLOYEE) {
            employees = List.of(user.getEmployee());
        } else {
            employees = employeeId != null
                    ? List.of(employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employee not found")))
                    : employeeRepository.findAll();
        }

        List<WeeklyTimelineDto> result = new ArrayList<>();

        for (Employee emp : employees) {

            Map<LocalDate, Attendance> attendanceMap = attendanceRepository
                    .findWeeklyAttendance(emp.getId(), start, end)
                    .stream()
                    .collect(Collectors.toMap(
                            Attendance::getDate,
                            a -> a,
                            (a1, a2) -> a1));

            Set<LocalDate> holidayDates = holidayRepository.findHolidayDatesBetween(start, end);

            for (int i = 0; i < 7; i++) {
                LocalDate date = start.plusDays(i);
                DayOfWeek day = date.getDayOfWeek();

                Attendance a = attendanceMap.get(date);

                boolean isHoliday = holidayDates.contains(date);

                String status;

                if (a != null) {
                    status = a.getStatus().name();
                } else if (isHoliday) {
                    status = Attendance.AttendanceStatus.HOLIDAY.name();
                } else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    status = Attendance.AttendanceStatus.WEEKEND.name();
                } else {
                    status = Attendance.AttendanceStatus.ABSENT.name();
                }

                if (a != null) {
                    result.add(mapToTimelineDto(a));
                } else {
                    result.add(new WeeklyTimelineDto(
                            emp.getId(),
                            emp.getFirstName(),
                            emp.getLastName(),
                            emp.getEmployeeId(),
                            date,
                            null,
                            null,
                            status,
                            0,
                            0,
                            false,
                            null));
                }
            }
        }

        return result;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

        int present = 0, halfDay = 0, leave = 0, weekend = 0, holiday = 0;

        Map<LocalDate, Attendance> map = records.stream().collect(Collectors.toMap(
                Attendance::getDate,
                a -> a,
                (a1, a2) -> a1));
        Set<LocalDate> holidayDates = holidayRepository.findHolidayDatesBetween(start, end);

        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            boolean isHoliday = holidayDates.contains(date);

            if (isHoliday) {
                holiday++;
                continue;
            }

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

        int present = 0, halfDay = 0, leave = 0, weekend = 0, holiday = 0;

        Map<LocalDate, Attendance> map = records.stream().collect(Collectors.toMap(
                Attendance::getDate,
                a -> a,
                (a1, a2) -> a1));
        Set<LocalDate> holidayDates = holidayRepository.findHolidayDatesBetween(start, end);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            DayOfWeek day = date.getDayOfWeek();
            boolean isHoliday = holidayDates.contains(date);
            if (isHoliday) {
                holiday++;
                continue;
            }

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
                        : 0,
                isLate(a),

                a.getWorkMode() != null
                        ? a.getWorkMode().name()
                        : null);
    }

}

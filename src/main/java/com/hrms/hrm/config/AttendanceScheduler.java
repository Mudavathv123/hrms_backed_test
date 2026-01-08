package com.hrms.hrm.config;

import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.service.impl.AttendanceServiceImpl;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceServiceImpl attendanceService;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    // Run every day at 23:59
    @Scheduled(cron = "0 59 23 * * ?")
    public void autoCheckoutDaily() {
        attendanceService.autoCheckoutEndOfDay();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void createDailyAttendance() {

        LocalDate today = LocalDate.now();
        List<Employee> employees = employeeRepository.findAll();

        for (Employee emp : employees) {

            boolean exists = attendanceRepository
                    .existsByEmployeeIdAndDate(emp.getId(), today);

            if (!exists) {
                Attendance attendance = Attendance.builder()
                        .employee(emp)
                        .date(today)
                        .status(Attendance.AttendanceStatus.ABSENT)
                        .build();

                attendanceRepository.save(attendance);
            }
        }
    }

}

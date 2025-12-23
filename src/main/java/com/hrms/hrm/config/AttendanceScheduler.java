package com.hrms.hrm.config;

import com.hrms.hrm.service.impl.AttendanceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceServiceImpl attendanceService;

    // Run every day at 23:59
    @Scheduled(cron = "0 59 23 * * ?")
    public void autoCheckoutDaily() {
        attendanceService.autoCheckoutEndOfDay();
    }
}

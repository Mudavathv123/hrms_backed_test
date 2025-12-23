package com.hrms.hrm.service;

import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.Payroll;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.LeaveRepository;
import com.hrms.hrm.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final AttendanceRepository attendanceRepo;
    private final LeaveRepository leaveRepo;
    private final PayrollRepository payrollRepo;
    private final EmployeeRepository employeeRepo;

    private static final double PF_PERCENT = 0.12;
    private static final double OT_MULTIPLIER = 1.5;

    public Payroll generatePayroll(UUID employeeId, int year, int month) {

        // LOCK CHECK
        payrollRepo.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .ifPresent(p -> {
                    if (p.getStatus() == Payroll.PayrollStatus.LOCKED)
                        throw new RuntimeException("Payroll is locked");
                });

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> attendance =
                attendanceRepo.findByEmployeeIdAndDateBetween(employeeId, start, end);

        List<Leave> leaves =
                leaveRepo.findByEmployeeIdAndStatus(employeeId, Leave.LeaveStatus.APPROVED);

        int totalDays = start.lengthOfMonth();
        int presentDays = (int) attendance.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT)
                .count();

        int unpaidLeaveDays = leaves.stream()
                .filter(l -> l.getLeaveType() == Leave.LeaveType.UNPAID)
                .mapToInt(Leave::getDays)
                .sum();

        int paidLeaveDays = leaves.stream()
                .filter(l -> l.getLeaveType() != Leave.LeaveType.UNPAID)
                .mapToInt(Leave::getDays)
                .sum();

        long overtimeMinutes = attendance.stream()
                .mapToLong(a -> a.getOvertimeMinutes() == null ? 0 : a.getOvertimeMinutes())
                .sum();

        double perDaySalary = emp.getSalary() / totalDays;
        double unpaidDeduction = unpaidLeaveDays * perDaySalary;

        double hourlyRate = emp.getSalary() / (totalDays * 8);
        double overtimeAmount = (overtimeMinutes / 60.0) * hourlyRate * OT_MULTIPLIER;

        double pf = emp.getSalary() * PF_PERCENT;
        double professionalTax = 200;

        double grossSalary =
                (presentDays + paidLeaveDays) * perDaySalary + overtimeAmount;

        double totalDeductions = pf + professionalTax + unpaidDeduction;

        double netSalary = grossSalary - totalDeductions;

        Payroll payroll = Payroll.builder()
                .employee(emp)
                .month(month)
                .year(year)
                .basicSalary(emp.getSalary())
                .perDaySalary(perDaySalary)
                .totalWorkingDays(totalDays)
                .presentDays(presentDays)
                .paidLeaveDays(paidLeaveDays)
                .unpaidLeaveDays(unpaidLeaveDays)
                .overtimeMinutes(overtimeMinutes)
                .overtimeAmount(overtimeAmount)
                .pfAmount(pf)
                .professionalTax(professionalTax)
                .unpaidLeaveDeduction(unpaidDeduction)
                .grossSalary(grossSalary)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .status(Payroll.PayrollStatus.GENERATED)
                .generatedOn(LocalDate.now())
                .build();

        return payrollRepo.save(payroll);
    }

    public void lockPayroll(UUID payrollId) {
        Payroll p = payrollRepo.findById(payrollId)
                .orElseThrow();
        p.setStatus(Payroll.PayrollStatus.LOCKED);
        payrollRepo.save(p);
    }
}

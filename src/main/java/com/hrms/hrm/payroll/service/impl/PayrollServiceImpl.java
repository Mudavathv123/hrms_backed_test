package com.hrms.hrm.payroll.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Leave;
import com.hrms.hrm.payroll.config.PayslipPdfGenerator;
import com.hrms.hrm.payroll.model.PaySlip;
import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.hrms.hrm.payroll.model.SalaryStructure;
import com.hrms.hrm.payroll.repository.PayrollDeductionRepository;
import com.hrms.hrm.payroll.repository.PayrollRepository;
import com.hrms.hrm.payroll.repository.PayslipRepository;
import com.hrms.hrm.payroll.repository.SalaryStructureRepository;
import com.hrms.hrm.payroll.service.PayrollService;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.LeaveRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

        private final PayrollRepository payrollRepository;
        private final SalaryStructureRepository salaryStructureRepository;
        private final AttendanceRepository attendanceRepository;
        private final LeaveRepository leaveRepository;
        private final PayrollDeductionRepository payrollDeductionRepository;
        private final PayslipRepository payslipRepository;

        private static final int WORKING_DAYS = 22;

        @Override
        public Payroll generatePayroll(UUID employeeId, int month, int year) {

                // Prevent duplicate payroll
                payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                                .ifPresent(p -> {
                                        throw new IllegalStateException("Payroll already generated for this month");
                                });

                // Fetch salary structure
                SalaryStructure salary = salaryStructureRepository.findByEmployeeId(employeeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found"));

                // Attendance & Leave
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                int presentDays = attendanceRepository.countPresentDays(employeeId, month, year);
                int unpaidLeaves = leaveRepository.countUnpaidLeaveDays(
                                employeeId,
                                Leave.LeaveType.UNPAID,
                                Leave.LeaveStatus.APPROVED,
                                monthStart,
                                monthEnd);

                int paidLeaveDays = leaveRepository.sumLeaveDays(
                                employeeId,
                                Leave.LeaveType.PAID,
                                Leave.LeaveStatus.APPROVED,
                                monthStart,
                                monthEnd);

                int unpaidLeaveDays = leaveRepository.sumLeaveDays(
                                employeeId,
                                Leave.LeaveType.UNPAID,
                                Leave.LeaveStatus.APPROVED,
                                monthStart,
                                monthEnd);

                int absentDays = WORKING_DAYS - presentDays - paidLeaveDays - unpaidLeaveDays;
                if (absentDays < 0)
                        absentDays = 0;

                if (paidLeaveDays < 0)
                        paidLeaveDays = 0;

                int lopDays = unpaidLeaveDays + absentDays;

                // Salary calculation
                BigDecimal monthlySalary = salary.getBasic()
                                .add(salary.getHra())
                                .add(salary.getAllowance());

                BigDecimal perDaySalary = monthlySalary.divide(
                                BigDecimal.valueOf(WORKING_DAYS),
                                2,
                                RoundingMode.HALF_UP);

                BigDecimal lopAmount = perDaySalary.multiply(BigDecimal.valueOf(lopDays));
                BigDecimal grossSalary = monthlySalary.subtract(lopAmount);

                BigDecimal pf = salary.getBasic()
                                .multiply(salary.getPfPercent())
                                .divide(BigDecimal.valueOf(100));

                BigDecimal tax = grossSalary
                                .multiply(salary.getTaxPercent())
                                .divide(BigDecimal.valueOf(100));

                BigDecimal totalDeduction = pf.add(tax).add(lopAmount);
                BigDecimal netSalary = grossSalary.subtract(totalDeduction);

                // Save Payroll
                Payroll payroll = new Payroll();
                payroll.setEmployeeId(employeeId);
                payroll.setMonth(month);
                payroll.setYear(year);
                payroll.setWorkingDays(WORKING_DAYS);
                payroll.setPresentDays(presentDays);
                payroll.setGrossSalary(grossSalary);
                payroll.setTotalDeductions(totalDeduction);
                payroll.setNetSalary(netSalary);
                payroll.setPaidLeaveDays(Math.max(paidLeaveDays, 0));
                payroll.setUnpaidLeaveDays(unpaidLeaveDays);
                payroll.setStatus(Payroll.PayrollStatus.GENERATED);

                payroll = payrollRepository.save(payroll);

                // Save deductions
                saveDeduction(payroll, "PF", pf);
                saveDeduction(payroll, "TAX", tax);
                saveDeduction(payroll, "LOSS_OF_PAY", lopAmount);

                // Generate Payslip PDF
                List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollId(payroll.getId());

                String employeeName = "Employee-" + employeeId; 

                try {
                        String pdfPath = PayslipPdfGenerator.generatePayslip(
                                        payroll, salary, deductions, employeeName);

                        PaySlip paySlip = new PaySlip();
                        paySlip.setPayroll(payroll);
                        paySlip.setPdfUrl(pdfPath);
                        payslipRepository.save(paySlip);

                } catch (Exception e) {
                        throw new RuntimeException("Payslip PDF generation failed", e);
                }

                return payroll;
        }

        private void saveDeduction(Payroll payroll, String type, BigDecimal amount) {
                PayrollDeduction deduction = new PayrollDeduction();
                deduction.setPayroll(payroll);
                deduction.setDeductionType(type);
                deduction.setAmount(amount);
                payrollDeductionRepository.save(deduction);
        }
}

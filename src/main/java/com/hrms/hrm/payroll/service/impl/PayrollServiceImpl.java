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
import com.hrms.hrm.payroll.model.Payroll.PayrollStatus;
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

                // 1️⃣ Prevent duplicate payroll
                payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                                .ifPresent(p -> {
                                        throw new IllegalStateException("Payroll already generated for this month");
                                });

                // 2️⃣ Fetch salary structure
                SalaryStructure salary = salaryStructureRepository.findByEmployeeId(employeeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found"));

                // 3️⃣ Attendance & Leave
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                int presentDays = attendanceRepository.countPresentDays(employeeId, month, year);
                int unpaidLeaves = leaveRepository.countUnpaidLeaveDays(
                                employeeId,
                                Leave.LeaveType.UNPAID,
                                Leave.LeaveStatus.APPROVED,
                                monthStart,
                                monthEnd);

                int lopDays = unpaidLeaves;

                // 4️⃣ Salary calculation
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

                // 5️⃣ Save Payroll
                Payroll payroll = new Payroll();
                payroll.setEmployeeId(employeeId);
                payroll.setMonth(month);
                payroll.setYear(year);
                payroll.setWorkingDays(WORKING_DAYS);
                payroll.setPresentDays(presentDays);
                payroll.setGrossSalary(grossSalary);
                payroll.setTotalDeductions(totalDeduction);
                payroll.setNetSalary(netSalary);
                payroll.setStatus(Payroll.PayrollStatus.GENERATED);

                payroll = payrollRepository.save(payroll);

                // 6️⃣ Save deductions
                saveDeduction(payroll, "PF", pf);
                saveDeduction(payroll, "TAX", tax);
                saveDeduction(payroll, "LOSS_OF_PAY", lopAmount);

                // 7️⃣ Generate Payslip PDF
                List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollId(payroll.getId());

                String employeeName = "Employee-" + employeeId; // replace later

                try {
                        String pdfPath = PayslipPdfGenerator.generatePayslip(
                                        payroll, deductions, employeeName);

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

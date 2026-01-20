package com.hrms.hrm.payroll.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hrms.hrm.error.PayrollException;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.User;
import com.hrms.hrm.payroll.config.MonthlyPayslipPdfGenerator;
import com.hrms.hrm.payroll.dto.DashboardStats;
import com.hrms.hrm.payroll.dto.PayrollDashboardResponse;
import com.hrms.hrm.payroll.dto.PayrollHistoryResponseDto;
import com.hrms.hrm.payroll.dto.PieChartDTO;
import com.hrms.hrm.payroll.dto.SalaryTrendDTO;
import com.hrms.hrm.payroll.model.EmployeeBankDetails;
import com.hrms.hrm.payroll.model.PaySlip;
import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.Payroll.PayrollStatus;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.hrms.hrm.payroll.model.SalaryStructure;
import com.hrms.hrm.payroll.repository.PayrollDeductionRepository;
import com.hrms.hrm.payroll.repository.PayrollRepository;
import com.hrms.hrm.payroll.repository.PayslipRepository;
import com.hrms.hrm.payroll.repository.SalaryStructureRepository;
import com.hrms.hrm.payroll.service.PayrollService;
import com.hrms.hrm.repository.AttendanceRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.LeaveRepository;
import com.hrms.hrm.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PayrollServiceImpl implements PayrollService {

        private final PayrollRepository payrollRepository;
        private final SalaryStructureRepository salaryStructureRepository;
        private final AttendanceRepository attendanceRepository;
        private final LeaveRepository leaveRepository;
        private final PayrollDeductionRepository payrollDeductionRepository;
        private final PayslipRepository payslipRepository;
        private final MonthlyPayslipPdfGenerator payslipPdfGenerator;
        private final EmployeeRepository employeeRepository;
        private final UserRepository userRepository;

        private static final int WORKING_DAYS = 22;

        @Override
        public Payroll generatePayroll(UUID employeeId, int month, int year) {

                payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                                .ifPresent(p -> {
                                        throw new IllegalStateException("Payroll already generated for this month");
                                });

                SalaryStructure salary = salaryStructureRepository.findByEmployeeId(employeeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found"));

                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

                /* ================= ATTENDANCE ================= */

                int presentDays = attendanceRepository.countPresentDays(employeeId, month, year);

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

                int paidDays = presentDays + paidLeaveDays;

                if (paidDays == 0) {
                        throw new PayrollException("Payroll cannot be generated when Paid Days is 0");
                }

                int lopDays = WORKING_DAYS - paidDays;

                if (lopDays < 0)
                        lopDays = 0;

                /* ================= SALARY ================= */

                // 1. Monthly gross
                BigDecimal monthlyGross = salary.getBasic()
                                .add(salary.getHra())
                                .add(salary.getAllowance());

                // 2. Per day salary
                BigDecimal perDaySalary = monthlyGross.divide(
                                BigDecimal.valueOf(WORKING_DAYS),
                                2,
                                RoundingMode.HALF_UP);

                // 3. LOP
                BigDecimal lopAmount = perDaySalary.multiply(BigDecimal.valueOf(lopDays));

                // 4. Earned gross (after LOP)
                BigDecimal earnedGross = monthlyGross.subtract(lopAmount);

                // 5. Earned BASIC for PF
                BigDecimal perDayBasic = salary.getBasic()
                                .divide(BigDecimal.valueOf(WORKING_DAYS), 2, RoundingMode.HALF_UP);

                BigDecimal earnedBasic = perDayBasic.multiply(BigDecimal.valueOf(paidDays));

                // 6. PF
                BigDecimal pf = BigDecimal.ZERO;
                if (salary.getPfPercent().compareTo(BigDecimal.ZERO) > 0) {
                        pf = earnedBasic
                                        .multiply(salary.getPfPercent())
                                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                // 7. Tax
                BigDecimal tax = earnedGross
                                .multiply(salary.getTaxPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                // 8. Total deductions (NO LOP here)
                BigDecimal totalDeductions = pf.add(tax);

                // 9. Net salary
                BigDecimal netSalary = earnedGross.subtract(totalDeductions);

                /* ================= PAYROLL ENTITY ================= */

                Payroll payroll = new Payroll();
                payroll.setEmployeeId(employeeId);
                payroll.setMonth(month);
                payroll.setYear(year);
                payroll.setPayPeriodStart(monthStart);
                payroll.setPayPeriodEnd(monthEnd);

                payroll.setTotalWorkingDays(WORKING_DAYS);
                payroll.setWorkingDays(presentDays + paidLeaveDays);
                payroll.setPresentDays(presentDays);
                payroll.setPaidLeaveDays(paidLeaveDays);
                payroll.setUnpaidLeaveDays(unpaidLeaveDays);
                payroll.setPaidDays(paidDays);

                // Salary snapshot
                payroll.setBasicSalary(salary.getBasic());
                payroll.setHra(salary.getHra());
                payroll.setAllowance(salary.getAllowance());

                payroll.setGrossSalary(earnedGross);
                payroll.setTotalDeductions(totalDeductions);
                payroll.setLopAmount(lopAmount);
                payroll.setArrears(BigDecimal.ZERO);
                payroll.setNetSalary(netSalary);

                /* ===== Bank & Statutory Snapshot (FIXED) ===== */
                if (employee.getBankDetails() != null) {
                        EmployeeBankDetails bank = employee.getBankDetails();

                        payroll.setBankName(bank.getBankName());
                        payroll.setBankAccountNumber(bank.getAccountNumber());
                        payroll.setIfscCode(bank.getIfscCode());

                        payroll.setUanNumber(bank.getUan());
                        // PAN optional if you add to Payroll later
                }

                payroll.setStatus(PayrollStatus.GENERATED);
                payroll = payrollRepository.save(payroll);

                /* ================= DEDUCTIONS ================= */

                if (pf.compareTo(BigDecimal.ZERO) > 0) {
                        saveDeduction(payroll, "PF", pf);
                }

                if (tax.compareTo(BigDecimal.ZERO) > 0) {
                        saveDeduction(payroll, "TAX", tax);
                }

                /* ================= PAYSLIP ================= */

                List<PayrollDeduction> deductions = payrollDeductionRepository.findByPayrollId(payroll.getId());

                try {
                        String pdfPath = payslipPdfGenerator.generatePayslip(
                                        payroll, salary, deductions);

                        PaySlip slip = new PaySlip();
                        slip.setPayroll(payroll);
                        slip.setPdfUrl(pdfPath);
                        payslipRepository.save(slip);

                } catch (Exception e) {
                        throw new RuntimeException("Payslip PDF generation failed", e);
                }

                return payroll;
        }

        private void saveDeduction(Payroll payroll, String type, BigDecimal amount) {
                PayrollDeduction d = new PayrollDeduction();
                d.setPayroll(payroll);
                d.setDeductionType(type);
                d.setAmount(amount);
                payrollDeductionRepository.save(d);
        }

        @Override
        public Page<PayrollHistoryResponseDto> getPayrollHistory(int page, int size) {

                Pageable pageable = PageRequest.of(page, size);
                Page<Payroll> payrollPage = payrollRepository.findAll(pageable);

                return payrollPage.map(payroll -> {

                        Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Employee not found with id: " + payroll.getEmployeeId()));

                        return PayrollHistoryResponseDto.builder()
                                        .payroll(payroll)
                                        .firstName(employee.getFirstName())
                                        .lastName(employee.getLastName())
                                        .email(employee.getEmail())
                                        .employeeCode(employee.getEmployeeId())
                                        .department(employee.getDepartment().getName())
                                        .designation(employee.getDesignation())
                                        .build();
                });
        }

        @Override
        public PayrollDashboardResponse getDashboard(int year) {
                List<Payroll> payrolls = payrollRepository.findByYear(year);

                long totalEmployees = payrolls.stream()
                                .map(Payroll::getEmployeeId)
                                .distinct()
                                .count();

                long pendingApprovals = payrolls.stream()
                                .filter(p -> p.getStatus() != PayrollStatus.PENDING_APPROVAL)
                                .count();

                double totalPaid = payrolls.stream()
                                .mapToDouble(p -> Math.max(p.getNetSalary().doubleValue(), 0))
                                .sum();

                DashboardStats stats = DashboardStats.builder()
                                .totalEmployees(totalEmployees)
                                .totalPayrolls(payrolls.size())
                                .pendingApprovals(pendingApprovals)
                                .totalPaid(totalPaid)
                                .build();

                SalaryTrendDTO firstHalf = buildTrend(payrolls, 1, 6, "Jan-Jun");
                SalaryTrendDTO secondHalf = buildTrend(payrolls, 7, 12, "Jul-Dec");

                BigDecimal grossTotal = payrolls.stream()
                                .map(Payroll::getGrossSalary)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDeductions = payrolls.stream()
                                .map(Payroll::getTotalDeductions)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<PieChartDTO> pie = List.of(
                                new PieChartDTO("Gross Salary", grossTotal.doubleValue()),
                                new PieChartDTO("Deductions", totalDeductions.doubleValue()));

                return PayrollDashboardResponse.builder()
                                .stats(stats)
                                .trends(List.of(firstHalf, secondHalf))
                                .salaryDistribution(pie)
                                .build();
        }

        private SalaryTrendDTO buildTrend(
                        List<Payroll> payrolls,
                        int startMonth,
                        int endMonth,
                        String label) {
                BigDecimal salary = payrolls.stream()
                                .filter(p -> p.getMonth() >= startMonth && p.getMonth() <= endMonth)
                                .map(Payroll::getGrossSalary)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal tax = payrolls.stream()
                                .filter(p -> p.getMonth() >= startMonth && p.getMonth() <= endMonth)
                                .map(Payroll::getTotalDeductions)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return SalaryTrendDTO.builder()
                                .period(label)
                                .salary(salary)
                                .tax(tax)
                                .build();
        }

        @Override
        public PayrollDashboardResponse getEmployeeDashboard(UUID employeeId, int year) {

                List<Payroll> payrolls = payrollRepository.findByEmployeeIdAndYear(employeeId, year);

                double totalPaid = payrolls.stream()
                                .mapToDouble(p -> Math.max(p.getNetSalary().doubleValue(), 0))
                                .sum();

                long totalPayrolls = payrolls.size();

                long pendingApprovals = 0;

                DashboardStats stats = DashboardStats.builder()
                                .totalEmployees(1)
                                .totalPayrolls(totalPayrolls)
                                .pendingApprovals(pendingApprovals)
                                .totalPaid(totalPaid)
                                .build();

                SalaryTrendDTO firstHalf = buildTrend(payrolls, 1, 6, "Jan-Jun");
                SalaryTrendDTO secondHalf = buildTrend(payrolls, 7, 12, "Jul-Dec");

                BigDecimal grossTotal = payrolls.stream()
                                .map(Payroll::getGrossSalary)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalDeductions = payrolls.stream()
                                .map(Payroll::getTotalDeductions)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<PieChartDTO> pie = List.of(
                                new PieChartDTO("Gross Salary", grossTotal.doubleValue()),
                                new PieChartDTO("Deductions", totalDeductions.doubleValue()));

                return PayrollDashboardResponse.builder()
                                .stats(stats)
                                .trends(List.of(firstHalf, secondHalf))
                                .salaryDistribution(pie)
                                .build();
        }

        @Override
        public void approvePayroll(UUID payrollId) {

                String email = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

                User approver = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

                if (!(approver.getRole() == User.Role.ROLE_ADMIN || approver.getRole() == User.Role.ROLE_HR)) {
                        throw new ResourceNotFoundException("You are not allowed to approve payroll");
                }

                Payroll payroll = payrollRepository.findById(payrollId)
                                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

                if (payroll.getStatus() == PayrollStatus.APPROVED) {
                        throw new IllegalStateException("Payroll already approved");
                }

                payroll.setStatus(PayrollStatus.APPROVED);
                payroll.setApprovedBy(approver);
                payroll.setApprovedAt(LocalDateTime.now());

                payrollRepository.save(payroll);

                log.info("Payroll {} approved by employee {}", payrollId);
        }

}

package com.hrms.hrm.payroll.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.hrms.hrm.payroll.dto.PayrollDashboardResponse;
import com.hrms.hrm.payroll.dto.PayrollHistoryResponseDto;
import com.hrms.hrm.payroll.model.Payroll;

public interface PayrollService {

    Payroll generatePayroll(UUID employeeId, int month, int year);

    Page<PayrollHistoryResponseDto> getPayrollHistory(int page, int size);

    PayrollDashboardResponse getDashboard(int year);

    PayrollDashboardResponse getEmployeeDashboard(UUID employeeId, int year);

    void approvePayroll(UUID payrollId);

}

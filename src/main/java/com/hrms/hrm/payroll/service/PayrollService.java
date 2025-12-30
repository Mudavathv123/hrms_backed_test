package com.hrms.hrm.payroll.service;

import java.util.UUID;

import com.hrms.hrm.payroll.model.Payroll;

public interface PayrollService {

    Payroll generatePayroll(UUID employeeId, int month, int year);

}

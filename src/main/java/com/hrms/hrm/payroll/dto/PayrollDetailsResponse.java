package com.hrms.hrm.payroll.dto;

import java.util.List;

import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.PayrollDeduction;

import lombok.Data;

@Data
public class PayrollDetailsResponse {

    private Payroll payroll;

    private List<PayrollDeduction> deductions;

}

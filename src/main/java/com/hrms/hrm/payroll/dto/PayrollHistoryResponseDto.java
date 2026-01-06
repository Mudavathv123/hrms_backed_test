package com.hrms.hrm.payroll.dto;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.payroll.model.Payroll;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayrollHistoryResponseDto {

    private Payroll payroll;
    private String firstName;
    private String lastName;
    private String designation;
    private String department;
    private String employeeCode;
    private String email;

}

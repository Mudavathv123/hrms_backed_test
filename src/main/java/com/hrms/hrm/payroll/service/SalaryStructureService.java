package com.hrms.hrm.payroll.service;

import com.hrms.hrm.payroll.dto.SalaryStructureRequest;
import com.hrms.hrm.payroll.dto.SalaryStructureResponse;

import java.util.List;
import java.util.UUID;

public interface SalaryStructureService {

    SalaryStructureResponse create(SalaryStructureRequest request);
    SalaryStructureResponse update(UUID employeeId, SalaryStructureRequest request);
    SalaryStructureResponse getByEmployeeId(UUID employeeId);
    List<SalaryStructureResponse> getAll();
}

package com.hrms.hrm.payroll.util;

import java.util.UUID;

import com.hrms.hrm.payroll.dto.SalaryStructureRequest;
import com.hrms.hrm.payroll.dto.SalaryStructureResponse;
import com.hrms.hrm.payroll.model.SalaryStructure;

public class DtoMapper {

    public static SalaryStructure toEntity(SalaryStructureRequest request) {
        return SalaryStructure.builder()
                .employeeId(UUID.fromString(request.getEmployeeId()))
                .basic(request.getBasic())
                .hra(request.getHra())
                .allowance(request.getAllowance())
                .pfPercent(request.getPfPercent())
                .taxPercent(request.getTaxPercent())
                .build();
    }

    public static SalaryStructureResponse toDto(SalaryStructure salary) {
        return SalaryStructureResponse.builder()
                .id(salary.getId())
                .employeeId(salary.getEmployeeId())
                .basic(salary.getBasic())
                .hra(salary.getHra())
                .allowance(salary.getAllowance())
                .pfPercent(salary.getPfPercent())
                .taxPercent(salary.getTaxPercent())
                .build();
    }

}

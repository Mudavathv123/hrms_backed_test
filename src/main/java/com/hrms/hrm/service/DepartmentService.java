package com.hrms.hrm.service;

import com.hrms.hrm.dto.DepartmentRequestDto;
import com.hrms.hrm.dto.DepartmentResponseDto;
import com.hrms.hrm.dto.EmployeeRequestDto;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    List<DepartmentResponseDto> getAllDepartments();

    DepartmentResponseDto createDepartment(DepartmentRequestDto request);

    DepartmentResponseDto updateDepartment(DepartmentRequestDto request, UUID uuid);

    Void deleteDepartmentById(UUID uuid);
}

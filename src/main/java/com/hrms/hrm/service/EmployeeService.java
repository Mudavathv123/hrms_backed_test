package com.hrms.hrm.service;

import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.dto.EmployeeResponseDto;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    List<EmployeeResponseDto> getAllEmployees();

    EmployeeResponseDto createEmployee(EmployeeRequestDto request);

    EmployeeResponseDto updateEmployee(EmployeeRequestDto request, UUID id);

    EmployeeResponseDto getEmployeeById(UUID id);

    Void deleteEmployeeById(UUID id);
}

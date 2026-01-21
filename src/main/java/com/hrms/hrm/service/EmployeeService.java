package com.hrms.hrm.service;

import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.dto.EmployeeResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    List<EmployeeResponseDto> getAllEmployees();

    List<EmployeeResponseDto> getInactiveEmployees();

    List<EmployeeResponseDto> getActiveEmployees();

    EmployeeResponseDto createEmployee(EmployeeRequestDto request);

    EmployeeResponseDto updateEmployee(EmployeeRequestDto request, UUID id);

    EmployeeResponseDto getEmployeeById(UUID id);

    Void deleteEmployeeById(UUID id);

    List<EmployeeResponseDto> getEmployeeByDepartments(String id);

    EmployeeResponseDto uploadAvatar(String employeeId, MultipartFile file);

}

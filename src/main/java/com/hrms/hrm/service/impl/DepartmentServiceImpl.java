package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.DepartmentRequestDto;
import com.hrms.hrm.dto.DepartmentResponseDto;
import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Department;
import com.hrms.hrm.repository.DepartmentRepository;
import com.hrms.hrm.service.DepartmentService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    @Override
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DtoMapper::toDto).toList();
    }

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto request) {
        Department department = departmentRepository.save(DtoMapper.toEntity(request));
        return DtoMapper.toDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartment(DepartmentRequestDto request, UUID uuid) {
        Department department = departmentRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("Department not found with id : "+uuid));

        if(request.getName() != null)
            department.setName(request.getName());
        department = departmentRepository.save(department);

        return DtoMapper.toDto(department);
    }

    @Override
    public Void deleteDepartmentById(UUID uuid) {
        departmentRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("Department not found with id : " +uuid));
        departmentRepository.deleteById(uuid);
        return null;
    }
}

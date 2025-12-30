package com.hrms.hrm.payroll.service.impl;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.payroll.dto.SalaryStructureRequest;
import com.hrms.hrm.payroll.dto.SalaryStructureResponse;
import com.hrms.hrm.payroll.model.SalaryStructure;
import com.hrms.hrm.payroll.repository.SalaryStructureRepository;
import com.hrms.hrm.payroll.service.SalaryStructureService;
import com.hrms.hrm.payroll.util.DtoMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;

    @Override
    public SalaryStructureResponse create(SalaryStructureRequest request) {

        if (salaryStructureRepository.existsByEmployeeId(UUID.fromString(request.getEmployeeId()))) {
            throw new ResourceNotFoundException("Salary structure already exists for employee");
        }

        SalaryStructure salary = DtoMapper.toEntity(request);

        return DtoMapper.toDto(salaryStructureRepository.save(salary));

    }

    @Override
    public SalaryStructureResponse update(UUID employeeId, SalaryStructureRequest request) {

        SalaryStructure existing = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found!"));

        existing.setBasic(request.getBasic());
        existing.setHra(request.getHra());
        existing.setAllowance(request.getAllowance());
        existing.setPfPercent(request.getPfPercent());
        existing.setTaxPercent(request.getTaxPercent());

        return DtoMapper.toDto(salaryStructureRepository.save(existing));
    }

    @Override
    public SalaryStructureResponse getByEmployeeId(UUID employeeId) {

        return salaryStructureRepository.findByEmployeeId(employeeId)
                .map(DtoMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found!"));
    }

    @Override
    public List<SalaryStructureResponse> getAll() {
        return salaryStructureRepository.findAll()
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}

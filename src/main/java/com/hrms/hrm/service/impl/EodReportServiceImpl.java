package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.EodRequestDto;
import com.hrms.hrm.dto.EodResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.EodReport;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.EodReportRepository;
import com.hrms.hrm.service.EodReportService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EodReportServiceImpl implements EodReportService {

    private final EodReportRepository eodReportRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public EodResponseDto createEod(EodRequestDto request) {

        Employee emp = employeeRepository.findById(UUID.fromString(request.getEmployeeId())
        )                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id:," +request.getEmployeeId()));

        String employeeFullName = emp.getFirstName() +" " +emp.getLastName();
        EodReport report = EodReport.builder()
                .employeeName(employeeFullName)
                .employeeCode(request.getEmployeeCode())
                .employeeId(request.getEmployeeId())
                .date(request.getDate())
                .workSummary(request.getWorkSummary())
                .blockers(request.getBlockers())
                .status(EodReport.Status.valueOf(request.getStatus().toUpperCase()))
                .build();

        EodReport saved = eodReportRepository.save(report);
        return DtoMapper.toDto(saved);
    }

    @Override
    public EodResponseDto updateEod(UUID id, EodRequestDto request) {

        EodReport report = eodReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EOD not found with id: " + id));

        if (request.getEmployeeCode() != null) report.setEmployeeCode(request.getEmployeeCode());
        if (request.getEmployeeId() != null) report.setEmployeeId(request.getEmployeeId());
        if (request.getDate() != null) report.setDate(request.getDate());
        if (request.getWorkSummary() != null) report.setWorkSummary(request.getWorkSummary());
        if (request.getBlockers() != null) report.setBlockers(request.getBlockers());
        if (request.getStatus() != null) report.setStatus(EodReport.Status.valueOf(request.getStatus().toUpperCase()));

        EodReport updated = eodReportRepository.save(report);
        return DtoMapper.toDto(updated);
    }

    @Override
    public List<EodResponseDto> getEmployeeEods(String employeeId) {
        return eodReportRepository.findByEmployeeId(employeeId)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<EodResponseDto> getAllEods() {
        return eodReportRepository.findAll()
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public void deleteEod(UUID id) {
        eodReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EOD not found with id: " + id));

        eodReportRepository.deleteById(id);
    }
}


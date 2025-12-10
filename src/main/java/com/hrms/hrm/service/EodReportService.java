package com.hrms.hrm.service;

import com.hrms.hrm.dto.EodRequestDto;
import com.hrms.hrm.dto.EodResponseDto;

import java.util.List;
import java.util.UUID;

public interface EodReportService {

    EodResponseDto createEod(EodRequestDto request);

    EodResponseDto updateEod(UUID id, EodRequestDto request);

    List<EodResponseDto> getEmployeeEods(String employeeId);

    List<EodResponseDto> getAllEods();

    void deleteEod(UUID id);
}

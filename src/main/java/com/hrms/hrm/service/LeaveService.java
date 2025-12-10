package com.hrms.hrm.service;

import com.hrms.hrm.dto.LeaveActionRequestDto;
import com.hrms.hrm.dto.LeaveRequestDto;
import com.hrms.hrm.dto.LeaveResponseDto;

import java.util.List;
import java.util.UUID;

public interface LeaveService {

    LeaveResponseDto applyLeave(LeaveRequestDto request);

    LeaveResponseDto actOnLeave(UUID leaveId, LeaveActionRequestDto actionRequest);

    List<LeaveResponseDto> getLeavesForEmployee(UUID employeeId);

    List<LeaveResponseDto> getPendingLeaves();

    List<LeaveResponseDto> getAllLeaves();
}

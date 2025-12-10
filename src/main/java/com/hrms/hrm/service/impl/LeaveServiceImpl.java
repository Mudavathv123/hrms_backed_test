package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.LeaveActionRequestDto;
import com.hrms.hrm.dto.LeaveRequestDto;
import com.hrms.hrm.dto.LeaveResponseDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.Leave.LeaveStatus;
import com.hrms.hrm.model.Leave.LeaveType;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.LeaveRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.LeaveService;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    // ---------------- APPLY LEAVE --------------------
    @Override
    public LeaveResponseDto applyLeave(LeaveRequestDto request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(LeaveType.valueOf(request.getLeaveType().toUpperCase()))
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .appliedOn(LocalDate.now())
                .build();

        Leave saved = leaveRepository.save(leave);

        // Send notification to all admins
        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);

        admins.forEach(admin -> {
            notificationService.createNotification(
                    NotificationRequestDto.builder()
                            .type("INFO")
                            .date(LocalDate.now())
                            .title("New Leave Request")
                            .message(employee.getFirstName() + " " + employee.getLastName() +
                                    " applied leave from " + request.getStartDate() +
                                    " to " + request.getEndDate())
                            .senderId(employee.getId())
                            .receiverId(admin.getEmployee().getId())
                            .targetRole("ADMIN")
                            .build()
            );
        });

        return DtoMapper.toDto(saved);
    }

    // ---------------- MANAGER/HR/ADMIN ACTIONS --------------------
    @Override
    public LeaveResponseDto actOnLeave(UUID leaveId, LeaveActionRequestDto actionRequest) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

        // Identify acting user (manager/hr/admin)
        Employee actor = employeeRepository.findById(actionRequest.getActorEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Actor (Manager/HR/Admin) not found"));

        String action = actionRequest.getAction().toUpperCase();

        switch (action) {
            case "APPROVE" -> leave.setStatus(LeaveStatus.APPROVED);
            case "REJECT" -> leave.setStatus(LeaveStatus.REJECTED);
            case "CANCEL" -> leave.setStatus(LeaveStatus.CANCELLED);
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        }

        leave.setManagerComment(actionRequest.getComment());
        leave.setActionOn(LocalDate.now());

        Leave updated = leaveRepository.save(leave);

        // ********** SEND NOTIFICATION TO EMPLOYEE **********
        String msg = "Your leave request has been " + leave.getStatus() +
                " by " + actor.getFirstName() + " (" + actor.getDesignation() + ")";

        notificationService.createNotification(
                NotificationRequestDto.builder()
                        .type("ALERT")
                        .title("Leave " + leave.getStatus())
                        .date(LocalDate.now())
                        .message(msg)
                        .senderId(actor.getId())                 // Manager/HR/Admin
                        .receiverId(leave.getEmployee().getId()) // Employee receiving notification
                        .targetRole("ADMIN")
                        .build()
        );

        return DtoMapper.toDto(updated);
    }

    @Override
    public List<LeaveResponseDto> getLeavesForEmployee(UUID employeeId) {
        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDto> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDto> getAllLeaves() {
        return leaveRepository.findAll()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }
}

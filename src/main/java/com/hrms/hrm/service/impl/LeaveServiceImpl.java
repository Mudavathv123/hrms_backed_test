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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

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

        Leave savedLeave = leaveRepository.save(leave);
        log.info("Leave applied - Employee: {} | Period: {} to {}",
                employee.getId(), request.getStartDate(), request.getEndDate());


        List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);
        admins.forEach(admin -> {
            try {
                if (admin.getEmployee() == null) {
                    log.warn("Admin {} has no employee mapping, skipping notification", admin.getId());
                    return;
                }

                NotificationRequestDto notification = NotificationRequestDto.builder()
                        .type("INFO")
                        .title("New Leave Request")
                        .message(String.format("%s %s applied leave from %s to %s",
                                employee.getFirstName(), employee.getLastName(),
                                request.getStartDate(), request.getEndDate()))
                        .date(LocalDate.now())
                        .senderId(employee.getId())
                        .receiverId(admin.getEmployee().getId())
                        .targetRole("ROLE_ADMIN")
                        .build();

                notificationService.sendNotification(notification);
                log.debug("Notification sent to admin userId: {}", admin.getId());
            } catch (Exception e) {
                log.error("Failed to send leave notification to admin {}: {}", admin.getId(), e.getMessage(), e);
            }
        });

        return DtoMapper.toDto(savedLeave);
    }

    @Override
    public LeaveResponseDto actOnLeave(UUID leaveId, LeaveActionRequestDto actionRequest) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found"));

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

        Leave updatedLeave = leaveRepository.save(leave);
        log.info("Leave action processed - LeaveID: {}, Action: {}, Status: {}",
                leaveId, action, leave.getStatus());

        try {
            if (leave.getEmployee() == null) {
                log.warn("Leave has null employee reference. Cannot send notification.");
                return DtoMapper.toDto(updatedLeave);
            }

            String msg = "Your leave request has been " + leave.getStatus() +
                    " by " + actor.getFirstName() + " (" + actor.getDesignation() + ")";

            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .type("ALERT")
                    .title("Leave " + leave.getStatus())
                    .date(LocalDate.now())
                    .message(msg)
                    .senderId(actor.getId())
                    .receiverId(leave.getEmployee().getId())
                    .targetRole("ROLE_EMPLOYEE")
                    .build();

            notificationService.sendNotification(notification);
            log.debug("Leave action notification sent to employee: {}", leave.getEmployee().getId());
        } catch (Exception e) {
            log.error("Failed to send leave notification to employee: {}", e.getMessage(), e);
        }

        return DtoMapper.toDto(updatedLeave);
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

package com.hrms.hrm.util;

import com.hrms.hrm.dto.*;
import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.Department;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Task;
import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.Notification;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.model.EodReport;
import com.hrms.hrm.dto.EodResponseDto;

public class DtoMapper {

    /* ================= EMPLOYEE ================= */

    public static EmployeeResponseDto toDto(Employee employee) {
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .designation(employee.getDesignation())
                .joiningDate(employee.getJoiningDate())
                .dateOfBirth(employee.getDateOfBirth())
                .departmentName(
                        employee.getDepartment() != null
                                ? employee.getDepartment().getName()
                                : "Not Assigned"
                )
                .build();
    }

    public static Employee toEntity(EmployeeRequestDto request) {
        return Employee.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .salary(request.getSalary())
                .address(request.getAddress())
                .designation(request.getDesignation())
                .joiningDate(request.getJoiningDate())
                .dateOfBirth(request.getDateOfBirth())
                .build();
    }

    /* ================= DEPARTMENT ================= */

    public static DepartmentResponseDto toDto(Department department) {
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }

    public static Department toEntity(DepartmentRequestDto request) {
        return Department.builder()
                .name(request.getName())
                .build();
    }

    /* ================= ATTENDANCE ================= */

    public static AttendanceResponseDto toDto(Attendance attendance) {
        return AttendanceResponseDto.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .attendanceStatus(String.valueOf(attendance.getStatus()))
                .workedTime(attendance.getWorkedTime())
                .date(attendance.getDate())
                .employeeCode(attendance.getEmployee().getEmployeeId())
                .firstName(attendance.getEmployee().getFirstName())
                .lastName(attendance.getEmployee().getLastName())
                .build();
    }

    /* ================= TASK ================= */

    public static TaskResponseDto toDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .priority(task.getPriority().name())
                .status(task.getStatus().name())
                .assignedToEmployeeId(task.getAssignedTo().getId())
                .assignedToEmployeeName(task.getEmployeeName())
                .employeeCode(task.getAssignedTo().getEmployeeId())
                .build();
    }

    /* ================= LEAVE (if you use LeaveServiceImpl) ================= */

    public static LeaveResponseDto toDto(Leave leave) {
        return LeaveResponseDto.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeCode(leave.getEmployee().getEmployeeId())
                .employeeName(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .leaveType(leave.getLeaveType().name())
                .status(leave.getStatus().name())
                .reason(leave.getReason())
                .days(leave.getDays())
                .managerComment(leave.getManagerComment())
                .appliedOn(leave.getAppliedOn())
                .actionOn(leave.getActionOn())
                .build();
    }
    public static NotificationResponseDto toDto(Notification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .date(n.getDate())
                .read(n.isRead())
                .senderId(n.getSender().getId())
                .receiverId(n.getReceiver().getId())
                .senderName(n.getSender().getFirstName())
                .receiverName(n.getReceiver().getFirstName())
                .targetRole(n.getTargetRole().name())
                .build();
    }

    public static EodResponseDto toDto(EodReport eod) {
        return EodResponseDto.builder()
                .id(eod.getId())
                .employeeName(eod.getEmployeeName())
                .employeeCode(eod.getEmployeeCode())
                .employeeId(eod.getEmployeeId())
                .date(eod.getDate())
                .workSummary(eod.getWorkSummary())
                .blockers(eod.getBlockers())
                .status(eod.getStatus().name())
                .build();
    }

}

package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.LeaveActionRequestDto;
import com.hrms.hrm.dto.LeaveRequestDto;
import com.hrms.hrm.dto.LeaveResponseDto;
import com.hrms.hrm.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponseDto>> applyLeave(@RequestBody LeaveRequestDto request) {
        return ResponseEntity.ok(
                ApiResponse.success(leaveService.applyLeave(request), "Leave applied successfully"));
    }

    @PostMapping("/{leaveId}/action")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> actOnLeave(
            @PathVariable UUID leaveId,
            @RequestBody LeaveActionRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(leaveService.actOnLeave(leaveId, request), "Leave updated successfully"));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getLeavesForEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success(leaveService.getLeavesForEmployee(employeeId), "Employee leaves fetched successfully"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getPendingLeaves() {
        return ResponseEntity.ok(
                ApiResponse.success(leaveService.getPendingLeaves(), "Pending leaves fetched successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getAllLeaves() {
        return ResponseEntity.ok(
                ApiResponse.success(leaveService.getAllLeaves(), "All leaves fetched successfully"));
    }
}

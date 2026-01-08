package com.hrms.hrm.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hrms.hrm.service.WfhPolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/wfh")
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@RequiredArgsConstructor
public class WfhPolicyController {

    private final WfhPolicyService wfhPolicyService;

    // Enable WFH for ALL employees
    @PostMapping("/enable-global")
    public ResponseEntity<?> enableGlobalWFH() {
        wfhPolicyService.enableGlobalWFH();
        return ResponseEntity.ok("WFH enabled for all employees");
    }

    // Enable WFH for SPECIFIC employee
    @PostMapping("/enable/{employeeId}")
    public ResponseEntity<?> enableEmployeeWFH(@PathVariable UUID employeeId) {
        wfhPolicyService.enableEmployeeWFH(employeeId);
        return ResponseEntity.ok("WFH enabled for employee");
    }

    // Disable ALL WFH
    @PostMapping("/disable")
    public ResponseEntity<?> disableWFH() {
        wfhPolicyService.disableAllWFH();
        return ResponseEntity.ok("WFH disabled");
    }
}

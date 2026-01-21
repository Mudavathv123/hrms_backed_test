package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.dto.EmployeeResponseDto;
import com.hrms.hrm.dto.ResignEmployeeRequestDto;
import com.hrms.hrm.service.EmployeeService;
import com.hrms.hrm.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserServiceImpl userServiceImpl;

    private final EmployeeService employeeService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getActiveEmployees() {
        List<EmployeeResponseDto> activeEmployees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(
                ApiResponse.success(activeEmployees, "Active employees fetched successfully"));
    }

    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getInactiveEmployees() {
        List<EmployeeResponseDto> inactiveEmployees = employeeService.getInactiveEmployees();
        return ResponseEntity.ok(
                ApiResponse.success(inactiveEmployees, "Inactive employees fetched successfully"));
    }

    @GetMapping("/get-all-employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getAllEmployees() {
        return ResponseEntity
                .ok(ApiResponse.success(employeeService.getAllEmployees(), "All employees information fetched.."));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> createEmployee(@RequestBody EmployeeRequestDto request) {
        return ResponseEntity
                .ok(ApiResponse.success(employeeService.createEmployee(request), "Employee added successfully.."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> updateEmployee(@RequestBody EmployeeRequestDto request,
            @PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(request, UUID.fromString(id))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> getEmployeeById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(UUID.fromString(id)),
                "Employee information fetched successfully.."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.deleteEmployeeById(UUID.fromString(id)),
                "The employee deleted successfully..."));
    }

    @GetMapping("/department/{id}")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getEmployeeByDepartments(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeByDepartments(id)));
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> uploadAvatar(
            @PathVariable String id,
            @RequestParam("avatar") MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.uploadAvatar(id, file),
                        "Avatar uploaded successfully"));
    }

    @PostMapping("/resign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> resignEmployee(
            @RequestBody ResignEmployeeRequestDto request) {

        userServiceImpl.resignEmployee(
                request.getEmployeeId(),
                request.getResignationDate(),
                request.getReason());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> reactivate(@PathVariable("id") UUID id) {
        userServiceImpl.reactivateEmployee(id);
        return ResponseEntity.ok().build();
    }
}

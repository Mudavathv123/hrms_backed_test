package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.dto.EmployeeResponseDto;
import com.hrms.hrm.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/get-all-employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees(),"All employees information fetched.."));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> createEmployee(@RequestBody EmployeeRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.createEmployee(request), "Employee added successfully.."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> updateEmployee(@RequestBody EmployeeRequestDto request, @PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateEmployee(request, UUID.fromString(id))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>>  getEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(UUID.fromString(id)), "Employee information fetched successfully.."));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.deleteEmployeeById(UUID.fromString(id)), "The employee deleted successfully..."));
    }

    @GetMapping("/department/{id}")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDto>>> getEmployeeByDepartments(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeByDepartments(id)));
    }

    @PostMapping(
            value = "/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> uploadAvatar(
            @PathVariable String id,
            @RequestParam("avatar") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(employeeService.uploadAvatar(id, file),
                        "Avatar uploaded successfully")
        );
    }

}

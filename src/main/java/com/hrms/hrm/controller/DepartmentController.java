package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.DepartmentRequestDto;
import com.hrms.hrm.dto.DepartmentResponseDto;
import com.hrms.hrm.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments(),"All departments fetched successful"));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(@RequestBody DepartmentRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.createDepartment(request),"Department added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment(@RequestBody DepartmentRequestDto request, @PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.updateDepartment(request, UUID.fromString(id)),"Department information updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartmentById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.deleteDepartmentById(UUID.fromString(id)),"Department fetched successful"));
    }
}

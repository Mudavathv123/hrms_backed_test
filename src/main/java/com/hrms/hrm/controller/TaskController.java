package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.TaskRequestDto;
import com.hrms.hrm.dto.TaskResponseDto;
import com.hrms.hrm.dto.TaskStatusUpdateDto;
import com.hrms.hrm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // CREATE TASK
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@RequestBody TaskRequestDto request) {
        return ResponseEntity.ok(
                ApiResponse.success(taskService.createTask(request), "Task created successfully")
        );
    }

    // UPDATE TASK DETAILS
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTask(
            @PathVariable UUID taskId,
            @RequestBody TaskRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(taskService.updateTask(taskId, request), "Task updated successfully")
        );
    }

    // UPDATE TASK STATUS ONLY
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody TaskStatusUpdateDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(taskService.updateTaskStatus(taskId, request), "Task status updated successfully")
        );
    }

    // DELETE TASK
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Task deleted successfully")
        );
    }

    // GET TASKS ASSIGNED TO EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getTasksForEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(
                ApiResponse.success(taskService.getTasksForEmployee(employeeId), "Employee tasks fetched successfully")
        );
    }

    // GET ALL TASKS
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getAllTasks() {
        return ResponseEntity.ok(
                ApiResponse.success(taskService.getAllTasks(), "All tasks fetched successfully")
        );
    }
}

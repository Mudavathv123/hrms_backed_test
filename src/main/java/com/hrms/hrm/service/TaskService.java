package com.hrms.hrm.service;

import com.hrms.hrm.dto.TaskRequestDto;
import com.hrms.hrm.dto.TaskResponseDto;
import com.hrms.hrm.dto.TaskStatusUpdateDto;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    List<TaskResponseDto> getAllTasks();

    List<TaskResponseDto> getTasksForEmployee(UUID employeeId);

    TaskResponseDto createTask(TaskRequestDto request);

    TaskResponseDto updateTask(UUID taskId, TaskRequestDto request);

    TaskResponseDto updateTaskStatus(UUID taskId, TaskStatusUpdateDto request);

    void deleteTask(UUID taskId);

}

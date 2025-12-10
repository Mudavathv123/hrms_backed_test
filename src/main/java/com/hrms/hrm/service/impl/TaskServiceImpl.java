package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.TaskRequestDto;
import com.hrms.hrm.dto.TaskResponseDto;
import com.hrms.hrm.dto.TaskStatusUpdateDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Task;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.TaskRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.service.TaskService;
import com.hrms.hrm.util.DtoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ---------------------------------------------------
    // Get logged-in employee as sender
    // ---------------------------------------------------
    private Employee getLoggedInEmployee() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        log.warn("User in  task service: {} - Attempt {}/{}", user);
        return user.getEmployee();
    }

    // ---------------------------------------------------
    // CREATE TASK
    // ---------------------------------------------------
    @Override
    public TaskResponseDto createTask(TaskRequestDto request) {

        Employee employee = employeeRepository.findById(UUID.fromString(request.getAssignedToEmployeeId()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getAssignedToEmployeeId()));

        Employee sender = getLoggedInEmployee(); // ADMIN or MANAGER who created

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .priority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()))
                .status(Task.TaskStatus.TODO)
                .assignedTo(employee)
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .build();

        Task saved = taskRepository.save(task);

        // Notify employee
        notificationService.createNotification(
                NotificationRequestDto.builder()
                        .type("TASK")
                        .title("New Task Assigned")
                        .date(LocalDate.now())
                        .message("A new task '" + task.getTitle() + "' has been assigned to you by "
                                + sender.getFirstName())
                        .senderId(sender.getId())     // REAL SENDER
                        .receiverId(employee.getId()) // EMPLOYEE
                        .targetRole("EMPLOYEE")
                        .build()
        );
        return DtoMapper.toDto(saved);
    }

    // ---------------------------------------------------
    // UPDATE TASK DETAILS
    // ---------------------------------------------------
    @Override
    public TaskResponseDto updateTask(UUID taskId, TaskRequestDto request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Employee sender = getLoggedInEmployee();

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());

        if (request.getDescription() != null)
            task.setDescription(request.getDescription());

        if (request.getStartDate() != null)
            task.setStartDate(request.getStartDate());

        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());

        if (request.getPriority() != null)
            task.setPriority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()));

        if (request.getAssignedToEmployeeId() != null) {
            Employee employee = employeeRepository.findById(UUID.fromString(request.getAssignedToEmployeeId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getAssignedToEmployeeId()));
            task.setAssignedTo(employee);
            task.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        }

        Task updated = taskRepository.save(task);

        // Notify updated employee
        notificationService.createNotification(
                NotificationRequestDto.builder()
                        .type("TASK")
                        .title("Task Updated")
                        .date(LocalDate.now())
                        .message("Your task '" + task.getTitle() + "' has been updated by "
                                + sender.getFirstName())
                        .senderId(sender.getId())
                        .receiverId(task.getAssignedTo().getId())
                        .targetRole("EMPLOYEE")
                        .build()
        );

        return DtoMapper.toDto(updated);
    }

    // ---------------------------------------------------
    // UPDATE TASK STATUS (EMPLOYEE MARK COMPLETE)
    // ---------------------------------------------------
    @Override
    public TaskResponseDto updateTaskStatus(UUID taskId, TaskStatusUpdateDto request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Employee sender = getLoggedInEmployee(); // who is performing the status update

        task.setStatus(Task.TaskStatus.valueOf(request.getStatus().toUpperCase()));
        Task updated = taskRepository.save(task);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        // Determine whom to notify
        if (user.getRole() == User.Role.ROLE_EMPLOYEE) {
            // Employee updated → notify Admins/Managers
            List<User> managers = userRepository.findByRole(User.Role.ROLE_ADMIN);
            managers.forEach(manager -> notificationService.createNotification(
                    NotificationRequestDto.builder()
                            .type("TASK")
                            .title("Task Status Updated")
                            .date(LocalDate.now())
                            .message("Task '" + task.getTitle() + "' updated to " + updated.getStatus()
                                    + " by " + sender.getFirstName())
                            .senderId(sender.getId())
                            .receiverId(manager.getEmployee().getId())
                            .targetRole("ADMIN") // Receiver role
                            .build()
            ));
        } else {
            // Admin/Manager updated → notify Employee
            Employee employee = task.getAssignedTo();
            notificationService.createNotification(
                    NotificationRequestDto.builder()
                            .type("TASK")
                            .title("Task Status Updated")
                            .date(LocalDate.now())
                            .message("Your task '" + task.getTitle() + "' updated to " + updated.getStatus()
                                    + " by " + sender.getFirstName())
                            .senderId(sender.getId())
                            .receiverId(employee.getId())
                            .targetRole("EMPLOYEE") // Receiver role
                            .build()
            );
        }

        return DtoMapper.toDto(updated);
    }

    // ---------------------------------------------------
    // DELETE TASK
    // ---------------------------------------------------
    @Override
    public void deleteTask(UUID taskId) {

        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.deleteById(taskId);
    }

    // ---------------------------------------------------
    // GET TASKS
    // ---------------------------------------------------
    @Override
    public List<TaskResponseDto> getTasksForEmployee(UUID employeeId) {
        return taskRepository.findByAssignedToId(employeeId)
                .stream().map(DtoMapper::toDto).toList();
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .stream().map(DtoMapper::toDto).toList();
    }
}

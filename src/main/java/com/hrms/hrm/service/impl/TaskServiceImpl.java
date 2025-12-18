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


    private Employee getLoggedInEmployee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        if (user.getEmployee() == null) {
            log.warn("Logged-in user {} has no employee mapping", user.getEmail());
            throw new ResourceNotFoundException("Employee not found for logged-in user");
        }

        return user.getEmployee();
    }


    @Override
    public TaskResponseDto createTask(TaskRequestDto request) {
        Employee assignedEmployee = employeeRepository.findById(UUID.fromString(request.getAssignedToEmployeeId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getAssignedToEmployeeId()));

        Employee sender = getLoggedInEmployee();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .priority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()))
                .status(Task.TaskStatus.TODO)
                .assignedTo(assignedEmployee)
                .employeeName(assignedEmployee.getFirstName() + " " + assignedEmployee.getLastName())
                .build();

        Task saved = taskRepository.save(task);


        notificationService.sendNotification(NotificationRequestDto.builder()
                .type("TASK")
                .title("New Task Assigned")
                .date(LocalDate.now())
                .message("A new task '" + task.getTitle() + "' has been assigned to you by "
                        + sender.getFirstName())
                .senderId(sender.getId())
                .receiverId(assignedEmployee.getId())
                .targetRole("ROLE_EMPLOYEE")
                .build());

        log.info("Task created and notification sent - TaskID: {}, AssignedTo: {}", saved.getId(), assignedEmployee.getId());
        return DtoMapper.toDto(saved);
    }


    @Override
    public TaskResponseDto updateTask(UUID taskId, TaskRequestDto request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Employee sender = getLoggedInEmployee();

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStartDate() != null) task.setStartDate(request.getStartDate());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) task.setPriority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()));

        if (request.getAssignedToEmployeeId() != null) {
            Employee employee = employeeRepository.findById(UUID.fromString(request.getAssignedToEmployeeId()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id: " + request.getAssignedToEmployeeId()));
            task.setAssignedTo(employee);
            task.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        }

        Task updated = taskRepository.save(task);


        notificationService.sendNotification(NotificationRequestDto.builder()
                .type("TASK")
                .title("Task Updated")
                .date(LocalDate.now())
                .message("Your task '" + task.getTitle() + "' has been updated by " + sender.getFirstName())
                .senderId(sender.getId())
                .receiverId(task.getAssignedTo().getId())
                .targetRole("ROLE_EMPLOYEE")
                .build());

        log.info("Task updated and notification sent - TaskID: {}", updated.getId());
        return DtoMapper.toDto(updated);
    }


    @Override
    public TaskResponseDto updateTaskStatus(UUID taskId, TaskStatusUpdateDto request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Employee sender = getLoggedInEmployee();
        task.setStatus(Task.TaskStatus.valueOf(request.getStatus().toUpperCase()));
        Task updated = taskRepository.save(task);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));

        if (user.getRole() == User.Role.ROLE_EMPLOYEE) {

            List<User> admins = userRepository.findByRole(User.Role.ROLE_ADMIN);
            admins.forEach(admin -> notificationService.sendNotification(NotificationRequestDto.builder()
                    .type("TASK")
                    .title("Task Status Updated")
                    .date(LocalDate.now())
                    .message("Task '" + task.getTitle() + "' updated to " + updated.getStatus()
                            + " by " + sender.getFirstName())
                    .senderId(sender.getId())
                    .receiverId(admin.getEmployee().getId())
                    .targetRole("ROLE_ADMIN")
                    .build()));
        } else {
            Employee employee = task.getAssignedTo();
            notificationService.sendNotification(NotificationRequestDto.builder()
                    .type("TASK")
                    .title("Task Status Updated")
                    .date(LocalDate.now())
                    .message("Your task '" + task.getTitle() + "' updated to " + updated.getStatus()
                            + " by " + sender.getFirstName())
                    .senderId(sender.getId())
                    .receiverId(employee.getId())
                    .targetRole("ROLE_EMPLOYEE")
                    .build());
        }

        log.info("Task status updated - TaskID: {}, Status: {}", task.getId(), updated.getStatus());
        return DtoMapper.toDto(updated);
    }

    @Override
    public void deleteTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
        log.info("Task deleted - TaskID: {}", taskId);
    }


    @Override
    public List<TaskResponseDto> getTasksForEmployee(UUID employeeId) {
        return taskRepository.findByAssignedToId(employeeId)
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}

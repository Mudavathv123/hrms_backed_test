package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.EmployeeRequestDto;
import com.hrms.hrm.dto.EmployeeResponseDto;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.error.EmployeeAlreadyExistException;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Department;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.DepartmentRepository;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.EmployeeService;
import com.hrms.hrm.service.FileStorageService;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final FileStorageService fileStorageService;

    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(Employee::getIsActive)
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {
        Employee existingEmployee = employeeRepository.findByEmail(request.getEmail());

        Department department = departmentRepository.findDepartmentByName(request.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with name: " + request.getDepartmentName()));

        if (existingEmployee != null) {
            throw new EmployeeAlreadyExistException("Employee already exists with email: " + request.getEmail());
        }

        Employee employee = DtoMapper.toEntity(request);
        employee.setDepartment(department);
        employee.setIsActive(true);
        employee.setResignationDate(null);

        employee = employeeRepository.save(employee);

        User.Role role = User.Role.ROLE_EMPLOYEE;

        if (request.getRole() != null)
            role = User.Role.valueOf("ROLE_" + request.getRole().toUpperCase());

        User user = User.builder()
                .role(role)
                .username(request.getFirstName() + " " + request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .employee(employee)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        try {
            notificationService.sendNotification(
                    NotificationRequestDto.builder()
                            .type("INFO")
                            .date(LocalDate.now())
                            .title("New Employee Added")
                            .message("Employee " + employee.getFirstName() + " " + employee.getLastName()
                                    + " has been added to department " + department.getName())
                            .senderId(null)
                            .receiverId(null)
                            .targetRole("ROLE_ADMIN")
                            .build());
            log.info("Notification sent for new employee: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notification for new employee {}: {}", employee.getEmail(), e.getMessage(), e);
        }

        return DtoMapper.toDto(employee);
    }

    @Override
    public EmployeeResponseDto updateEmployee(EmployeeRequestDto request, UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (!employee.getIsActive()) {
            throw new RuntimeException("Cannot update inactive employee");
        }

        Department department = departmentRepository.findDepartmentByName(request.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with name: " + request.getDepartmentName()));

        if (request.getEmployeeId() != null)
            employee.setEmployeeId(request.getEmployeeId());
        if (request.getFirstName() != null)
            employee.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            employee.setLastName(request.getLastName());
        if (request.getEmail() != null)
            employee.setEmail(request.getEmail());
        if (request.getPhone() != null)
            employee.setPhone(request.getPhone());
        if (request.getDesignation() != null)
            employee.setDesignation(request.getDesignation());
        if (request.getSalary() != null)
            employee.setSalary(request.getSalary());
        if (request.getJoiningDate() != null)
            employee.setJoiningDate(request.getJoiningDate());
        if (request.getDateOfBirth() != null)
            employee.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null)
            employee.setAddress(request.getAddress());
        if (request.getDepartmentName() != null)
            employee.setDepartment(department);

        employee = employeeRepository.save(employee);

        try {
            notificationService.sendNotification(
                    NotificationRequestDto.builder()
                            .type("INFO")
                            .date(LocalDate.now())
                            .title("Employee Updated")
                            .message("Employee " + employee.getFirstName() + " " + employee.getLastName()
                                    + " has been updated.")
                            .senderId(null)
                            .receiverId(null)
                            .targetRole("ROLE_ADMIN")
                            .build());
            log.info("Notification sent for updated employee: {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notification for updated employee {}: {}", employee.getEmail(), e.getMessage(),
                    e);
        }

        return DtoMapper.toDto(employee);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        return DtoMapper.toDto(emp);
    }

    @Override
    public Void deleteEmployeeById(UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setIsActive(false);
        employee.setResignationDate(LocalDate.now());
        employeeRepository.save(employee);

        User user = userRepository.findByEmployee(employee).orElse(null);
        if (user != null) {
            user.setIsActive(false);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        try {
            notificationService.sendNotification(
                    NotificationRequestDto.builder()
                            .type("ALERT")
                            .date(LocalDate.now())
                            .title("Employee Deactivated")
                            .message("Employee " + employee.getFirstName() + " " + employee.getLastName()
                                    + " has been marked inactive.")
                            .senderId(null)
                            .receiverId(null)
                            .targetRole("ROLE_ADMIN")
                            .build());
        } catch (Exception e) {
            log.error("Notification failed for employee deactivation {}", employee.getEmail(), e);
        }

        return null;
    }

    @Override
    public List<EmployeeResponseDto> getEmployeeByDepartments(String id) {

        Department department = departmentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id :" + id));

        return employeeRepository.findByDepartment(department)
                .stream()
                .filter(Employee::getIsActive)
                .map(DtoMapper::toDto)
                .toList();

    }

    @Override
    public EmployeeResponseDto uploadAvatar(String employeeId, MultipartFile file) {

        Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id " + employeeId));

        if (!employee.getIsActive()) {
            throw new ResourceNotFoundException("Inactive employee cannot update avatar");
        }

        if (file == null || file.isEmpty()) {
            throw new ResourceNotFoundException("Uploaded profile image is empty");
        }

        // Validate file size (max 5MB)
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (fileSizeInMB > 5) {
            throw new ResourceNotFoundException(
                    String.format("Profile image size exceeds 5MB limit. Current size: %dMB", fileSizeInMB));
        }

        // Validate file type (only JPEG and PNG)
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
            throw new ResourceNotFoundException(
                    "Invalid profile image type. Only JPEG and PNG are allowed");
        }

        try {
            // Delete old avatar if exists
            if (employee.getAvatar() != null && !employee.getAvatar().isBlank()) {
                fileStorageService.deleteFile(employee.getAvatar());
                log.info("Deleted old avatar for employee: {}", employeeId);
            }

            // Upload new profile image to S3 or local storage
            String fileUrl = fileStorageService.uploadFile(file);

            employee.setAvatar(fileUrl);
            employeeRepository.save(employee);

            log.info("Successfully uploaded profile image for employee: {}", employeeId);
            return DtoMapper.toDto(employee);

        } catch (ResourceNotFoundException e) {
            log.error("Validation error uploading avatar for employee {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload avatar for employee {}", employeeId, e);
            throw new RuntimeException("Failed to upload profile image: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EmployeeResponseDto> getActiveEmployees() {
        return employeeRepository.findAllByIsActiveTrue()
                .stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDto> getInactiveEmployees() {

        List<Employee> inactiveEmployees = employeeRepository.findAllByIsActiveFalse();

        return inactiveEmployees.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

}

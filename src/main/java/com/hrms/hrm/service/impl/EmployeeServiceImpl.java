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
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private  final NotificationService notificationService;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @Override
    public List<EmployeeResponseDto> getAllEmployees() {
       return employeeRepository.findAll().stream()
               .map(DtoMapper::toDto).toList();
    }

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {
        Employee existingEmployee = employeeRepository.findByEmail(request.getEmail());

        Department department = departmentRepository.findDepartmentByName(request.getDepartmentName())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name : " +request.getDepartmentName()));

        if(existingEmployee != null) throw new EmployeeAlreadyExistException("Employee is already exist with email : " +request.getEmail());

        Employee employee = DtoMapper.toEntity(request);
        employee.setDepartment(department);

        employee = employeeRepository.save(employee);

        User user = User.builder()
                .role(request.getRole())
                .username(request.getFirstName() +" " +request.getLastName())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .employee(employee)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        notificationService.createNotification(
                NotificationRequestDto.builder()
                        .type("INFO")
                        .date(LocalDate.now())
                        .title("New Employee Added")
                        .message("Employee " + employee.getFirstName() + " " + employee.getLastName() + " has been added.")
                        .build()
        );


        return DtoMapper.toDto(employee);

    }

    @Override
    public EmployeeResponseDto updateEmployee(EmployeeRequestDto request, UUID id) {

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee is not found with id: " +id));
        Department department = departmentRepository.findDepartmentByName(request.getDepartmentName()).orElseThrow(() -> new ResourceNotFoundException("Department is not found with name: " +request.getDepartmentName()));

        if(request.getEmployeeId() != null)
            employee.setEmployeeId(request.getEmployeeId());
        if(request.getFirstName() != null)
            employee.setFirstName(request.getFirstName());
        if(request.getLastName() != null)
            employee.setLastName(request.getLastName());
        if(request.getEmail() != null)
            employee.setEmail(request.getEmail());
        if(request.getPhone() != null)
            employee.setPhone(request.getPhone());
        if(request.getDesignation() != null)
            employee.setDesignation(request.getDesignation());
        if(request.getSalary() != null)
            employee.setSalary(request.getSalary());
        if(request.getJoiningDate() != null)
            employee.setJoiningDate(request.getJoiningDate());
        if(request.getDateOfBirth() != null)
            employee.setDateOfBirth(request.getDateOfBirth());
        if(request.getDepartmentName() != null)
            employee.setDepartment(department);

        employee = employeeRepository.save(employee);

        return DtoMapper.toDto(employee);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee emp = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("The employee not found with id : " +id));
        return DtoMapper.toDto(emp);
    }

    @Override
    public Void deleteEmployeeById(UUID id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // 1. Find user linked to this employee
        User user = userRepository.findByEmployee(employee).orElse(null);

        // 2. Delete the user first (to avoid FK constraint error)
        if (user != null) {
            user.setEmployee(null);      // unlink first to avoid problems
            userRepository.delete(user);
        }

        // 3. Delete employee record
        employeeRepository.delete(employee);
       return null;
    }
}

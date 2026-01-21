package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.ChangePasswordRequestDto;
import com.hrms.hrm.dto.UpdateProfileRequestDto;
import com.hrms.hrm.dto.UpdateProfileResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @Override
    public UpdateProfileResponseDto updateProfile(UpdateProfileRequestDto request) {

        User user = getCurrentUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Inactive user cannot update profile");
        }

        user.setUsername(request.getName());
        user.setEmail(request.getEmail());

        user = userRepository.save(user);
        return UpdateProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequestDto request) {
        User user = getCurrentUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Inactive user cannot change password");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {

        return userRepository.findById(getLoggedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + getLoggedUserId()));
    }

    private Long getLoggedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username : " + username))
                .getId();
    }

    @Transactional
    public void resignEmployee(UUID employeeId, LocalDate resignationDate, String reason) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setIsActive(false);
        employee.setResignationDate(resignationDate);
        employee.setResignationReason(reason);

        User user = userRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        user.setLockedUntil(LocalDateTime.now().plusYears(100));

        employeeRepository.save(employee);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateEmployee(UUID employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setIsActive(true);
        employee.setResignationDate(null);
        employee.setResignationReason(null);

        User user = userRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);

        employeeRepository.save(employee);
        userRepository.save(user);
    }

}

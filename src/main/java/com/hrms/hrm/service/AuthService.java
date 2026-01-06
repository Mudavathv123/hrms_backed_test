package com.hrms.hrm.service;

import com.hrms.hrm.dto.*;
import com.hrms.hrm.error.BadCredentialsException;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 10;


    public String generateResetToken(ForgotPasswordKeyRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found exception"));

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        return token;
    }


    public void restPassword(ResetPasswordRequestDto requestDto) {
        User user = userRepository.findByResetToken(requestDto.getResetToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if(user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Token expired.");

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadCredentialsException("Account locked. Try again later");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            userRepository.updateFailedLoginAttempts(user.getEmail(), 0);
            userRepository.lastLogin(user.getEmail(), LocalDateTime.now());

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String token = jwtUtil.generateToken(userDetails.getUsername(), user);

            UUID employeeId = user.getEmployee() != null ? user.getEmployee().getId() : null;

            return LoginResponseDto.builder()
                    .employee(user.getEmployee())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .employeeId(employeeId)
                    .token(token)
                    .refreshToken(token)
                    .expiresIn(86400L)
                    .role(user.getRole().name())
                    .build();

        } catch (Exception ex) {
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts() + 1);
            log.warn("Authentication failed for user: {} - Attempt {}/{}", user.getEmail(), attempts, MAX_FAILED_ATTEMPTS);

            userRepository.updateFailedLoginAttempts(user.getEmail(), attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
                log.warn("Max login attempts reached. Locking account for user: {} until {}", user.getEmail(), lockUntil);
                userRepository.lockUser(user.getEmail(), lockUntil);
            }

            log.error("Authentication error for user: {} - {}", user.getEmail(), ex.getMessage(), ex);
            throw ex;
        }
    }

    public SignupResponseDto signup(SignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already taken");
        }
        User.Role role;
        try {
            if (request.getRole() == null || request.getRole().isBlank()) {
                role = User.Role.ROLE_HR;
            } else {
                String normalized = request.getRole().trim().toUpperCase();
                if (!normalized.startsWith("ROLE_")) {
                    normalized = "ROLE_" + normalized;
                }
                role = User.Role.valueOf(normalized);

                if (!(role == User.Role.ROLE_ADMIN || role == User.Role.ROLE_HR || role == User.Role.ROLE_MANAGER)) {
                    throw new IllegalArgumentException("Only Admin, HR, Manager can signup");
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role. Allowed roles: Admin, HR, Manager");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        User savedUser = userRepository.save(newUser);

        if (role == User.Role.ROLE_ADMIN || role == User.Role.ROLE_HR || role == User.Role.ROLE_MANAGER) {
            int randomNumber = (int) (Math.random() * 900) + 100;
            String empId = "EMP-" + randomNumber;

            Employee employee = Employee.builder()
                    .employeeId(empId)
                    .firstName(request.getUsername())
                    .lastName("") 
                    .email(request.getEmail())
                    .phone("N/A")
                    .designation(role.name().replace("ROLE_", ""))
                    .salary(0.0)
                    .joiningDate(LocalDate.now())
                    .build();

            Employee savedEmployee = employeeRepository.save(employee);
            savedUser.setEmployee(savedEmployee);
            userRepository.save(savedUser);
        }

        return SignupResponseDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

}

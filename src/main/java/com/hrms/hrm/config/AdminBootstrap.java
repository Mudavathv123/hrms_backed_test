package com.hrms.hrm.config;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    @PostConstruct
    public void init() {

        createAdminIfNotExists(
                "admin@hrms.com",
                "Admin@123",
                "Admin",
                null 
        );

        createAdminIfNotExists(
                "vinodmudavath30@hrms.com",
                "Mangi@143",
                "Super Admin",
                UUID.fromString("a03830f6-ce13-46f6-b03f-32aae8715b52")
        );
    }

    private void createAdminIfNotExists(
            String email,
            String rawPassword,
            String username,
            UUID employeeId) {
        if (userRepository.existsByEmail(email))
            return;

        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(User.Role.ROLE_ADMIN);
        admin.setIsActive(true);
        admin.setUsername(username);

        // LINK EMPLOYEE IF PROVIDED
        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException(
                            "Employee not found with ID: " + employeeId));
            admin.setEmployee(employee);
        }

        userRepository.save(admin);
    }

}

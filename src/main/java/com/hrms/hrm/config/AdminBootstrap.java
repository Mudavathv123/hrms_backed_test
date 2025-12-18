package com.hrms.hrm.config;

import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void init() {
        if(userRepository.count() == 0) {
            User admin =  new User();
            admin.setEmail("admin@hrms.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ROLE_ADMIN);

            userRepository.save(admin);
        }
    }
}

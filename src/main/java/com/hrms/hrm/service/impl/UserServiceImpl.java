package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.ChangePasswordRequestDto;
import com.hrms.hrm.dto.UpdateProfileRequestDto;
import com.hrms.hrm.dto.UpdateProfileResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UpdateProfileResponseDto updateProfile(UpdateProfileRequestDto request) {

        User user = getCurrentUser();

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

        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if(!request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {

        return userRepository.findById(getLoggedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " +getLoggedUserId()));
    }

    private Long getLoggedUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username : " +username))
                .getId();
    }
}

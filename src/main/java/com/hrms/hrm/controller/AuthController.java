package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.LoginRequestDto;
import com.hrms.hrm.dto.LoginResponseDto;
import com.hrms.hrm.dto.SignupRequestDto;
import com.hrms.hrm.dto.SignupResponseDto;
import com.hrms.hrm.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getEmail());
        try {
            LoginResponseDto response = authService.login(request);
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            log.error("Login failed for user: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@RequestBody SignupRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request), "signup is successful"));
    }
}

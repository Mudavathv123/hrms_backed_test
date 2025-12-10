package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.ChangePasswordRequestDto;
import com.hrms.hrm.dto.UpdateProfileRequestDto;
import com.hrms.hrm.dto.UpdateProfileResponseDto;
import com.hrms.hrm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UpdateProfileResponseDto>> updateProfile(@RequestBody UpdateProfileRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(request), "profile updated"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully"));

    }
}

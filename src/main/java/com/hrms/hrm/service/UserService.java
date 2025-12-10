package com.hrms.hrm.service;

import com.hrms.hrm.dto.ChangePasswordRequestDto;
import com.hrms.hrm.dto.UpdateProfileRequestDto;
import com.hrms.hrm.dto.UpdateProfileResponseDto;

public interface UserService {
    UpdateProfileResponseDto updateProfile(UpdateProfileRequestDto request);

    void changePassword(ChangePasswordRequestDto request);
}

package com.hrms.hrm.dto;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}

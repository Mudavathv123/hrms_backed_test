package com.hrms.hrm.dto;

import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    private String resetToken;
    private String newPassword;
}

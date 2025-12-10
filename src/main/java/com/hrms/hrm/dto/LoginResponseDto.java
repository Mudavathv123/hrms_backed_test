package com.hrms.hrm.dto;

import com.hrms.hrm.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private String token;
    private String role;
    private UUID employeeId;
    private String refreshToken;
    private String username;
    private String email;
    private Long expiresIn;
    private Employee employee;
}

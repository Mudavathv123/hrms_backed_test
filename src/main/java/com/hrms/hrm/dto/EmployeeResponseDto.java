package com.hrms.hrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponseDto {

    private UUID id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String designation;
    private String email;
    private String phone;
    private String address;
    private String avatar;
    private LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private String departmentName;
    private Boolean isActive;
}

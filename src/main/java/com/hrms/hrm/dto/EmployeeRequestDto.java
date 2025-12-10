package com.hrms.hrm.dto;

import com.hrms.hrm.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeRequestDto {

    private String employeeId;
    private String firstName;
    private String lastName;
    private String designation;
    private String email;
    private String phone;
    private Double salary;
    private LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private String departmentName;

    private String password;
    private User.Role role = User.Role.ROLE_EMPLOYEE;
}

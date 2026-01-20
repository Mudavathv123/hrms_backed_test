package com.hrms.hrm.payroll.model;

import com.hrms.hrm.model.Employee;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_bank_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumber;

    private String ifscCode;
    private String branch;

    @Column(unique = true)
    private String uan;  

    @Column(unique = true)
    private String pan;  
    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}

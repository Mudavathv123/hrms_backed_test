package com.hrms.hrm.payroll.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.payroll.model.PayrollDeduction;

@Repository
public interface PayrollDeductionRepository extends JpaRepository<PayrollDeduction, UUID>{

    List<PayrollDeduction> findByPayrollId(UUID payrollId);

}

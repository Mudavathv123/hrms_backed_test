package com.hrms.hrm.payroll.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.payroll.model.PaySlip;

@Repository
public interface PayslipRepository extends JpaRepository<PaySlip, UUID> {

    Optional<PaySlip> findByPayrollId(UUID payrollId);
}

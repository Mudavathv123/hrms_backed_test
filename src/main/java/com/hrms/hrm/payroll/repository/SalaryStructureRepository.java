package com.hrms.hrm.payroll.repository;

import com.hrms.hrm.payroll.model.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {

    Optional<SalaryStructure> findByEmployeeId(UUID employeeId);

    Boolean existsByEmployeeId(UUID employeeId);
}

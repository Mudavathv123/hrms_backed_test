package com.hrms.hrm.repository;

import com.hrms.hrm.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    Optional<Payroll> findByEmployeeIdAndMonthAndYear(UUID empId, int month, int year);

    List<Payroll> findByMonthAndYear(int month, int year);
}

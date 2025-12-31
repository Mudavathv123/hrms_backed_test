package com.hrms.hrm.payroll.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.Payroll.PayrollStatus;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {

    Optional<Payroll> findByEmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    BigDecimal getTotalSalaryPaid(@Param("month") int month, @Param("year") int year);

    long countByStatus(Payroll.PayrollStatus string);

    List<Payroll> findByEmployeeId(UUID employeeId);

    List<Payroll> findByStatus(PayrollStatus status);

    Page<Payroll> findAll(Pageable pageable);

    Page<Payroll> findByStatus(Payroll.PayrollStatus status, Pageable pageable);

}

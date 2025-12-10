
package com.hrms.hrm.repository;

import com.hrms.hrm.model.EodReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EodReportRepository extends JpaRepository<EodReport, UUID> {

    List<EodReport> findByEmployeeId(String employeeId);

    List<EodReport> findByDate(LocalDate date);
}

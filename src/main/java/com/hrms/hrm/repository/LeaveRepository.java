package com.hrms.hrm.repository;

import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.Leave.LeaveStatus;
import com.hrms.hrm.model.Leave.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    List<Leave> findByEmployeeId(UUID employeeId);

    List<Leave> findByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);

    List<Leave> findByStatus(LeaveStatus status);

    @Query("""
                SELECT COALESCE(SUM(l.days), 0)
                FROM Leave l
                WHERE l.employee.id = :employeeId
                  AND l.leaveType = :leaveType
                  AND l.status = :status
                  AND l.startDate <= :monthEnd
                  AND l.endDate >= :monthStart
            """)
    int countUnpaidLeaveDays(
            @Param("employeeId") UUID employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("status") LeaveStatus status,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);
}

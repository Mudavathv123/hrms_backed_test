package com.hrms.hrm.repository;

import com.hrms.hrm.model.Leave;
import com.hrms.hrm.model.Leave.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    List<Leave> findByEmployeeId(UUID employeeId);

    List<Leave> findByEmployeeIdAndStatus(UUID employeeId, LeaveStatus status);

    List<Leave> findByStatus(LeaveStatus status);
}

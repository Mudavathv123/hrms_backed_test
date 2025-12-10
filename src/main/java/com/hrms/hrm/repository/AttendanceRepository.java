package com.hrms.hrm.repository;

import com.hrms.hrm.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByEmployeeId(UUID employeeId);

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetween(UUID employeeId, LocalDate start, LocalDate end);

    Optional<Attendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);
}

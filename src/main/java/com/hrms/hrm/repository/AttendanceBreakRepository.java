package com.hrms.hrm.repository;

import com.hrms.hrm.model.Attendance;
import com.hrms.hrm.model.AttendanceBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceBreakRepository extends JpaRepository<AttendanceBreak, UUID> {
    Optional<AttendanceBreak> findByAttendance(Attendance attendance);
}

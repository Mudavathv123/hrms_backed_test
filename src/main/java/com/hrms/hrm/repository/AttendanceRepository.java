package com.hrms.hrm.repository;

import com.hrms.hrm.model.Attendance;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByEmployeeId(UUID employeeId);

    List<Attendance> findByDate(LocalDate date);

    boolean existsByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :start AND :end")
    List<Attendance> findWeeklyAttendance(
            @Param("employeeId") UUID employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE a.date = :date AND a.checkInTime IS NOT NULL AND a.checkOutTime IS NULL")
    List<Attendance> findByDateAndCheckedInWithoutCheckout(@Param("date") LocalDate date);

    List<Attendance> findByDateLessThanEqualOrderByDateDesc(LocalDate date);

    Optional<Attendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetween(UUID employeeId, LocalDate start, LocalDate end);

    Optional<Attendance> findTopByEmployeeIdAndDateBeforeOrderByDateDesc(
            UUID employeeId,
            LocalDate date);

    @Query("""
                SELECT a
                FROM Attendance a
                WHERE a.date = (
                    SELECT MAX(a2.date)
                    FROM Attendance a2
                    WHERE a2.employee.id = a.employee.id
                    AND a2.date < :date
                )
            """)
    List<Attendance> findLatestAttendancePerEmployeeBefore(@Param("date") LocalDate date);

    @Query("""
                SELECT COUNT(a)
                FROM Attendance a
                WHERE a.employee.id = :employeeId
                  AND a.status = 'PRESENT'
                  AND MONTH(a.date) = :month
                  AND YEAR(a.date) = :year
            """)
    int countPresentDays(
            @Param("employeeId") UUID employeeId,
            @Param("month") int month,
            @Param("year") int year);

}

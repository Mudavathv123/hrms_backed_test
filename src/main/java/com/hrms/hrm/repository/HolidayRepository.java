package com.hrms.hrm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.model.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    boolean existsByDate(LocalDate date);

    List<Holiday> findByDateBetween(LocalDate start, LocalDate end);

    @Query("select h.date from Holiday h where h.date between :start and :end")
    Set<LocalDate> findHolidayDatesBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

}

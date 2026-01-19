package com.hrms.hrm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.hrm.model.Holiday;
import com.hrms.hrm.repository.HolidayRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public class HolidayController {

    private final HolidayRepository holidayRepository;

    @PostMapping
    public ResponseEntity<Holiday> addHoliday(@RequestBody Holiday holiday) {

        if (holidayRepository.existsByDate(holiday.getDate())) {
            throw new RuntimeException("Holiday already exists for this date");
        }

        return ResponseEntity.ok(
                holidayRepository.save(holiday));
    }

    @GetMapping
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }
}

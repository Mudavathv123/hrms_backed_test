package com.hrms.hrm.controller;

import org.springframework.web.bind.annotation.RestController;

import com.hrms.hrm.model.AllowedLocation;
import com.hrms.hrm.repository.AllowedLocationRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/admin/locations")
@PreAuthorize("hasAnyRole('ADMIN','HR')")
@RequiredArgsConstructor
public class AllowedLocationController {

    private final AllowedLocationRepository repository;

    @PostMapping
    public AllowedLocation create(@RequestBody AllowedLocation location) {
        return repository.save(location);
    }

    @GetMapping
    public List<AllowedLocation> getAll() {
        return repository.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        repository.deleteById(id);
    }
}

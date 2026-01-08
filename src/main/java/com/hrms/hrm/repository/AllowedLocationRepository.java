package com.hrms.hrm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.model.AllowedLocation;

@Repository
public interface AllowedLocationRepository extends JpaRepository<AllowedLocation, UUID> {

}

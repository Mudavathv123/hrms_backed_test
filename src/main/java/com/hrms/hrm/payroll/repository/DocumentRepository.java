package com.hrms.hrm.payroll.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.payroll.model.Document;

@Repository
public interface DocumentRepository  extends JpaRepository<Document, UUID> {

    List<Document> findByEmployeeId(UUID employeeId);
}

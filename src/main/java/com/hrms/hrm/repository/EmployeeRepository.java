package com.hrms.hrm.repository;

import com.hrms.hrm.model.Department;
import com.hrms.hrm.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Employee findByEmail(String email);

    List<Employee> findByDepartment(Department department);

    List<Employee> findAllByIsActiveTrue();

    List<Employee> findAllByIsActiveFalse();
}

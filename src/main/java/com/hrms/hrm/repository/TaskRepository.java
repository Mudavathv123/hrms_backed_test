package com.hrms.hrm.repository;

import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Task;
import com.hrms.hrm.model.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByAssignedToId(UUID employeeId);

    List<Task> findByStatus(TaskStatus status);

}


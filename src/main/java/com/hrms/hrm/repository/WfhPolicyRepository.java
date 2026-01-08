package com.hrms.hrm.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.model.WfhPolicy;

@Repository
public interface WfhPolicyRepository extends JpaRepository<WfhPolicy, UUID> {

    boolean existsByIsGlobalTrueAndEnabledTrue();

    boolean existsByEmployee_IdAndEnabledTrue(UUID employeeId);

    @Modifying
    @Query("UPDATE WfhPolicy w SET w.enabled = false")
    void disableAll();

}

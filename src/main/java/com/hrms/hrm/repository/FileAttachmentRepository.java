package com.hrms.hrm.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hrms.hrm.model.FileAttachment;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, UUID>{

    List<FileAttachment> findByReferenceId(UUID referenceId);
    List<FileAttachment> findByModuleAndReferenceId(String module, UUID referenceId);

}

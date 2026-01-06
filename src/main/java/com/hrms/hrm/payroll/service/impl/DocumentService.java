package com.hrms.hrm.payroll.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hrms.hrm.payroll.model.Document;
import com.hrms.hrm.payroll.repository.DocumentRepository;
import com.hrms.hrm.payroll.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public Document upload(
            MultipartFile file,
            String documentType,
            UUID employeeId,
            UUID uploadedBy) {

        String localPath = fileStorageService.saveToLocal(file);
        // String s3Url = fileStorageService.uploadToS3(file);

        Document document = Document.builder()
                .employeeId(employeeId)
                .contentType(file.getContentType())
                .documnetType(documentType)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .localPath(localPath)
                .s3Url(null)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .build();

        return documentRepository.save(document);

    }

    public List<Document> getDocuments(UUID employeeId) {
        return documentRepository.findByEmployeeId(employeeId);
    }

}

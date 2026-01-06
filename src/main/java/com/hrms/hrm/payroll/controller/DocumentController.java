package com.hrms.hrm.payroll.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;
import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.payroll.model.Document;
import com.hrms.hrm.payroll.repository.DocumentRepository;
import com.hrms.hrm.payroll.service.impl.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Document>> upload(

            @RequestParam MultipartFile file,
            @RequestParam String documentType,
            @RequestParam UUID employeeId,
            @RequestParam UUID uploadedBy) {
        return ResponseEntity.ok(ApiResponse.success(documentService.upload(file, documentType, employeeId, uploadedBy),
                "Document uploaded successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR', 'MANAGER', 'EMPLOYEE')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Document>>> getEmployeeDocs(@PathVariable UUID employeeId) {
        return ResponseEntity
                .ok(ApiResponse.success(documentService.getDocuments(employeeId), "Document fetched successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER','EMPLOYEE')")
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID documentId) throws Exception {

        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        Path path = Path.of(doc.getLocalPath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + doc.getFileName())
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(resource);

    }

}

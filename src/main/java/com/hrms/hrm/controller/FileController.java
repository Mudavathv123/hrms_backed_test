package com.hrms.hrm.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.FileAttachment;
import com.hrms.hrm.repository.FileAttachmentRepository;
import com.hrms.hrm.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileAttachmentRepository fileAttachmentRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("module") String module,
            @RequestParam("referenceId") UUID referenceId) {

        String fileUrl = fileStorageService.uploadFile(file);

        FileAttachment attachment = FileAttachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileUrl(fileUrl)
                .module(module)
                .referenceId(referenceId)
                .uploadedAt(Instant.now())
                .build();

        fileAttachmentRepository.save(attachment);

        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable UUID fileId) {

        FileAttachment file = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        String signedUrl = fileStorageService.generatePresinedUrl(file.getFileUrl());
        return ResponseEntity.ok(signedUrl);

    }

}

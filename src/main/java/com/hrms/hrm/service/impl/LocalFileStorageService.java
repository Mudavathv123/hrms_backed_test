package com.hrms.hrm.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.hrms.hrm.service.FileStorageService;

public class LocalFileStorageService implements FileStorageService {

    private final String uploadDir = "uploads";

    @Override
    public String uploadFile(MultipartFile file) {

        try {

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 Important for debugging
            throw new RuntimeException("Local upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresinedUrl(String fileName) {
        return "http://localhost:8080/uploads/" + fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }
}

package com.hrms.hrm.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file);

    void deleteFile(String fileUrl);

    String generatePresinedUrl(String fileName);

}

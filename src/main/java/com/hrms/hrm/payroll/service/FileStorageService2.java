package com.hrms.hrm.payroll.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService2 {

    String saveToLocal(MultipartFile file);

    String uploadToS3(MultipartFile file);

}

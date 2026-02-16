// package com.hrms.hrm.payroll.service.impl;

// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.hrms.hrm.error.ResourceNotFoundException;
// import com.hrms.hrm.payroll.service.FileStorageService;

// @Service
// @ConditionalOnProperty(
//         name = "file.storage",
//         havingValue = "local",
//         matchIfMissing = true
// )
// public class LocalFileStorageService implements FileStorageService2 {

//     @Value("${file.upload.dir}")
//     private String uploadDir;

//     @Override
//     public String saveToLocal(MultipartFile file) {
//         try {
//             Path dir = Paths.get(uploadDir);
//             Files.createDirectories(dir);

//             String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//             Path path = dir.resolve(fileName);

//             Files.write(path, file.getBytes());

//             return path.toString();

//         } catch (Exception e) {
//             throw new ResourceNotFoundException("Failed to store file locally " + e);
//         }
//     }

//     @Override
//     public String uploadToS3(MultipartFile file) {
//         throw new UnsupportedOperationException("S3 not enabled");
//     }
// }

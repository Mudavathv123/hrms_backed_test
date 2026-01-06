package com.hrms.hrm.payroll.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.payroll.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String saveToLocal(MultipartFile file) {

        try {

            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = dir.resolve(fileName);

            Files.write(path, file.getBytes());

            return path.toString();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to store file locally" + e);
        }
    }

    @Override
    public String uploadToS3(MultipartFile file) {

        // try {

        // String key = "documents/" + UUID.randomUUID() + "_" +
        // file.getOriginalFilename();

        // PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        // .bucket(bucketName)
        // .key(key)
        // .contentType(file.getContentType())
        // .build();

        // s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        // return s3Client.utilities()
        // .getUrl(b -> b.bucket(bucketName).key(key))
        // .toString();

        // } catch (Exception e) {

        // throw new ResourceNotFoundException("Failed to upload file to s3" + e);

        // }

        return null;
    }

}

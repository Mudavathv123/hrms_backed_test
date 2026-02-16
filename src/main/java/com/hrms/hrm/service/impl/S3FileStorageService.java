package com.hrms.hrm.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.presigner.PresignRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Production-ready AWS S3 file storage service
 * Handles profile images, payslips, and other documents with validation
 */
@Service
@ConditionalOnProperty(name = "file.storage", havingValue = "s3")
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region:ap-south-1}")
    private String region;

    // File size limits in bytes
    @Value("${file.image.max-size:5}")
    private int imageMaxSizeMB;

    @Value("${file.document.max-size:10}")
    private int documentMaxSizeMB;

    @Value("${file.payslip.max-size:20}")
    private int payslipMaxSizeMB;

    // Allowed MIME types
    @Value("${file.image.allowed-types:image/jpeg,image/png,image/jpg}")
    private String allowedImageTypes;

    @Value("${file.document.allowed-types:application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document}")
    private String allowedDocumentTypes;

    private static final String PROFILE_IMAGES_PREFIX = "profile-images/";
    private static final String DOCUMENTS_PREFIX = "documents/";
    private static final String PAYSLIPS_PREFIX = "payslips/";
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    /**
     * Upload profile image to S3
     * Max 5MB, only JPEG/PNG
     */
    public String uploadProfileImage(MultipartFile file) {
        try {
            validateImage(file);
            String fileName = generateFileName(PROFILE_IMAGES_PREFIX, file.getOriginalFilename());
            uploadToS3(file, fileName);
            log.info("Uploaded profile image: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload profile image", e);
            throw new ResourceNotFoundException("Failed to upload profile image: " + e.getMessage());
        }
    }

    /**
     * Upload document (PDF, Word) to S3
     * Max 10MB
     */
    public String uploadDocument(MultipartFile file) {
        try {
            validateDocument(file);
            String fileName = generateFileName(DOCUMENTS_PREFIX, file.getOriginalFilename());
            uploadToS3(file, fileName);
            log.info("Uploaded document: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            throw new ResourceNotFoundException("Failed to upload document: " + e.getMessage());
        }
    }

    /**
     * Upload payslip PDF to S3
     * Max 20MB
     */
    public String uploadPayslip(MultipartFile file) {
        try {
            validatePayslip(file);
            String fileName = generateFileName(PAYSLIPS_PREFIX, file.getOriginalFilename());
            uploadToS3(file, fileName);
            log.info("Uploaded payslip: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Failed to upload payslip", e);
            throw new ResourceNotFoundException("Failed to upload payslip: " + e.getMessage());
        }
    }

    /**
     * Generic file upload with type validation
     */
    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResourceNotFoundException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return uploadProfileImage(file);
            } else if (contentType.equals("application/pdf")) {
                return uploadDocument(file);
            }
        }

        return uploadDocument(file);
    }

    /**
     * Delete file from S3
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            if (fileName == null || fileName.isBlank()) {
                log.warn("Attempted to delete empty filename");
                return;
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from S3: {}", fileName);
        } catch (NoSuchKeyException e) {
            log.warn("File not found in S3: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileName, e);
            throw new ResourceNotFoundException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Generate presigned URL for secure file access
     * Valid for 15 minutes
     */
    @Override
    public String generatePresinedUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ResourceNotFoundException("Invalid file name");
        }

        try (S3Presigner presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            log.debug("Generated presigned URL for: {}", fileName);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", fileName, e);
            throw new ResourceNotFoundException("Failed to generate URL: " + e.getMessage());
        }
    }

    /**
     * Validate image file
     */
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Image file is empty");
        }

        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (fileSizeInMB > imageMaxSizeMB) {
            throw new ResourceNotFoundException(
                    String.format("Image size exceeds %dMB limit. Current size: %dMB", 
                    imageMaxSizeMB, fileSizeInMB));
        }

        List<String> allowedTypes = List.of(allowedImageTypes.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.trim())) {
            throw new ResourceNotFoundException(
                    String.format("Invalid image type. Allowed types: %s", allowedImageTypes));
        }
    }

    /**
     * Validate document file
     */
    private void validateDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Document file is empty");
        }

        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (fileSizeInMB > documentMaxSizeMB) {
            throw new ResourceNotFoundException(
                    String.format("Document size exceeds %dMB limit. Current size: %dMB", 
                    documentMaxSizeMB, fileSizeInMB));
        }

        List<String> allowedTypes = List.of(allowedDocumentTypes.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.trim())) {
            throw new ResourceNotFoundException(
                    String.format("Invalid document type. Allowed types: %s", allowedDocumentTypes));
        }
    }

    /**
     * Validate payslip file
     */
    private void validatePayslip(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Payslip file is empty");
        }

        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (fileSizeInMB > payslipMaxSizeMB) {
            throw new ResourceNotFoundException(
                    String.format("Payslip size exceeds %dMB limit. Current size: %dMB", 
                    payslipMaxSizeMB, fileSizeInMB));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ResourceNotFoundException("Payslip must be a PDF file");
        }
    }

    /**
     * Upload file to S3
     */
    private void uploadToS3(MultipartFile file, String fileName) throws Exception {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(
                file.getInputStream(), file.getSize()));
    }

    /**
     * Generate unique filename with prefix and UUID
     */
    private String generateFileName(String prefix, String originalFilename) {
        String sanitizedName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String ext = sanitizedName.substring(sanitizedName.lastIndexOf("."));
        return prefix + UUID.randomUUID() + ext;
    }

    /**
     * Get bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Get presigned URL with custom duration (in minutes)
     */
    public String generatePresignedUrlWithDuration(String fileName, int durationMinutes) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
            throw new ResourceNotFoundException("Failed to generate URL");
        }
    }

}

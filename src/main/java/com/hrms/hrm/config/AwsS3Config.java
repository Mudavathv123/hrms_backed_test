package com.hrms.hrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "file.storage", havingValue = "s3", matchIfMissing = false)
public class AwsS3Config {

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    /**
     * Create S3Client bean with production-ready configuration
     * Supports both IAM roles (EC2/ECS) and static credentials
     */
    @Bean
    public S3Client s3Client() {
        try {
            boolean useStaticCredentials = accessKey != null && !accessKey.isEmpty() && 
                                          secretKey != null && !secretKey.isEmpty();

            if (useStaticCredentials) {
                // Use static credentials (for development/testing)
                return S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                        .build();
            } else {
                // Use default credentials provider (IAM roles in production)
                return S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create S3Client", e);
        }
    }
}

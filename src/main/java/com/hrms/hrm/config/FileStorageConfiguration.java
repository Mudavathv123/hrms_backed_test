package com.hrms.hrm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.hrms.hrm.service.FileStorageService;
import com.hrms.hrm.service.impl.LocalFileStorageService;

@Configuration
public class FileStorageConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageService();
    }

    // S3FileStorageService is auto-wired as a @Service component
    // with @ConditionalOnProperty, so no bean definition needed here
}

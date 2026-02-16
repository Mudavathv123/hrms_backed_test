package com.hrms.hrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS, resource handlers, and static file serving
 * Supports both local file serving and S3 presigned URLs
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String corsOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String corsMethods;

    @Value("${cors.allow-credentials:true}")
    private Boolean corsAllowCredentials;

    @Value("${file.storage:local}")
    private String fileStorage;

    /**
     * Configure CORS for production-ready API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsOrigins.split(",");
        String[] methods = corsMethods.split(",");

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods(methods)
                .allowedHeaders("*")
                .allowCredentials(corsAllowCredentials)
                .maxAge(3600)
                .exposedHeaders("Authorization", "X-Requested-With", "Content-Disposition");

        // Allow CORS for file endpoints
        registry.addMapping("/uploads/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Configure resource handlers for local file serving
     * When file.storage=local, serve files from uploads directory
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from uploads directory (local storage only)
        if ("local".equalsIgnoreCase(fileStorage)) {
            registry
                    .addResourceHandler("/uploads/**")
                    .addResourceLocations("file:./uploads/")
                    .setCachePeriod(3600); // Cache for 1 hour
        }

        // Serve static company files (logos, documents)
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // Cache for 24 hours
    }
}


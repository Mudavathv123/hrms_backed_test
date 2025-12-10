package com.hrms.hrm.config;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {

    private Boolean success;
    private T data;
    private String message;
    private Instant timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String msg) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(msg)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String msg) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(msg)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(T data, String msg) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(msg)
                .timestamp(Instant.now())
                .build();
    }
}

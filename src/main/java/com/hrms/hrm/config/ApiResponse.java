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
    private Integer statusCode;
    private Instant timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(null)
                .statusCode(200)
                .timestamp(Instant.now())
                .build();
    }


    public static <T> ApiResponse<T> success(T data, String msg) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(msg)
                .statusCode(200)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String msg) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(msg)
                .statusCode(500)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(T data, String msg) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(msg)
                .statusCode(500)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String msg, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(msg)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(T data, String msg, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(msg)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .build();
    }
}

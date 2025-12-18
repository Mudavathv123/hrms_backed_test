package com.hrms.hrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class NotificationRequestDto {
    private UUID receiverId;

    @NotBlank @Size(max = 200)
    private String title;

    @NotBlank @Size(max = 2000)
    private String message;

    @NotBlank
    private String type;

    private String targetRole;

    private LocalDate date;

    private UUID senderId;
}

package com.hrms.hrm.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class NotificationRequestDto {
    private String title;
    private String message;
    private String type;         // TASK, LEAVE, SYSTEM, CUSTOM, EOD
    private String targetRole;   // EMPLOYEE, HR, MANAGER, ADMIN
    private LocalDate date;

    private UUID senderId;
    private UUID receiverId;     // Null → send to all users in targetRole
}

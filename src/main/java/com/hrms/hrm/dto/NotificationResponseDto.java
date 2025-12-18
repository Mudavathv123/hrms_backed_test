package com.hrms.hrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hrms.hrm.model.Notification;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Notification responses.
 * Used for API serialization and client-facing notification data.
 * 
 * PRODUCTION CONSIDERATIONS:
 * - Non-null fields are excluded from JSON response using @JsonInclude
 * - Proper date formatting for frontend consumption
 * - Null-safe conversions from entity to DTO
 */
@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String message;
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private boolean read;

    private UUID senderId;
    private String senderName;

    private UUID receiverId;
    private String receiverName;

    private String targetRole;


    public static NotificationResponseDto fromEntity(Notification n) {

        String senderName = null;
        if (n.getSender() != null) {

            String firstName = n.getSender().getFirstName() != null ? n.getSender().getFirstName() : "";
            String lastName = n.getSender().getLastName() != null ? n.getSender().getLastName() : "";
            senderName = String.join(" ", firstName, lastName).trim();

            if (senderName.isEmpty()) {
                senderName = null;
            }
        }

        String receiverName = null;
        if (n.getReceiver() != null) {

            String firstName = n.getReceiver().getFirstName() != null ? n.getReceiver().getFirstName() : "";
            String lastName = n.getReceiver().getLastName() != null ? n.getReceiver().getLastName() : "";
            receiverName = String.join(" ", firstName, lastName).trim();

            if (receiverName.isEmpty()) {
                receiverName = null;
            }
        }

        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType() != null ? n.getType().name() : null)
                .date(n.getDate())
                .createdDate(n.getCreatedDate())
                .read(n.isRead())
                .senderId(n.getSender() != null ? n.getSender().getId() : null)
                .senderName(senderName)
                .receiverId(n.getReceiver() != null ? n.getReceiver().getId() : null)
                .receiverName(receiverName)
                .targetRole(n.getTargetRole() != null ? n.getTargetRole().name() : null)
                .build();
    }
}

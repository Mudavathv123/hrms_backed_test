package com.hrms.hrm.dto;

import com.hrms.hrm.model.Notification;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private LocalDate date;
    private boolean read;

    private UUID senderId;
    private String senderName;

    private UUID receiverId;
    private String receiverName;

    private String targetRole;


    public static NotificationResponseDto fromEntity(Notification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .date(n.getDate())
                .read(n.isRead())

                .senderId(n.getSender() != null ? n.getSender().getId() : null)
                .senderName(n.getSender() != null
                        ? n.getSender().getFirstName() + " " + n.getSender().getLastName()
                        : null)

                .receiverId(n.getReceiver() != null ? n.getReceiver().getId() : null)
                .receiverName(n.getReceiver() != null
                        ? n.getReceiver().getFirstName() + " " + n.getReceiver().getLastName()
                        : null)

                .targetRole(n.getTargetRole() != null ? n.getTargetRole().name() : null)
                .build();
    }

}

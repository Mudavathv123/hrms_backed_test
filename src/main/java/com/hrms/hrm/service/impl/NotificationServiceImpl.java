package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Notification;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.NotificationRepository;
import com.hrms.hrm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    @Transactional
    public NotificationResponseDto sendNotification(NotificationRequestDto req) {

        Employee sender = null;
        if (req.getSenderId() != null) {
            sender = employeeRepository.findById(req.getSenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sender with ID: " + req.getSenderId() + " not found"));
        }


        Employee receiver = null;
        if (req.getReceiverId() != null) {
            receiver = employeeRepository.findById(req.getReceiverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver with ID: " + req.getReceiverId() + " not found"));
        }


        Notification.NotificationType notificationType;
        try {
            notificationType = Notification.NotificationType.valueOf(req.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid notification type: {}", req.getType());
            throw new IllegalArgumentException("Invalid notification type. Must be one of: INFO, WARNING, ALERT");
        }


        Notification.TargetRole targetRole = null;
        if (req.getTargetRole() != null) {
            try {
                targetRole = Notification.TargetRole.valueOf(req.getTargetRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid target role: {}", req.getTargetRole());
                throw new IllegalArgumentException("Invalid target role. Must be one of: ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE");
            }
        }


        LocalDateTime now = LocalDateTime.now();
        
        Notification n = Notification.builder()
                .title(req.getTitle())
                .message(req.getMessage())
                .type(notificationType)
                .date(req.getDate() != null ? req.getDate() : LocalDate.now())
                .read(false)
                .sender(sender)
                .receiver(receiver)
                .targetRole(targetRole)
                .createdDate(now)
                .build();

        Notification saved = notificationRepository.save(n);
        

        NotificationResponseDto dto = NotificationResponseDto.fromEntity(saved);


        try {
            if (receiver != null) {

                String receiverEmail = receiver.getEmail();
                if (receiverEmail != null && !receiverEmail.isEmpty()) {
                    messagingTemplate.convertAndSendToUser(
                            receiverEmail,
                            "/queue/notifications",
                            dto
                    );
                    log.debug("Notification sent via WebSocket to user: {}", receiverEmail);
                }
            } else if (targetRole != null) {

                messagingTemplate.convertAndSend(
                        "/topic/notifications/" + targetRole.name(),
                        dto
                );
                log.debug("Notification sent via WebSocket to role: {}", targetRole.name());
            }
        } catch (Exception ex) {

            log.error("Failed to send WebSocket notification (ID: {}). Message will be available via REST endpoint. Error: {}",
                    saved.getId(), ex.getMessage(), ex);
        }

        return dto;
    }


    @Override
    public List<NotificationResponseDto> getUnreadNotifications(UUID employeeId) {
        // ISSUE FIXED: Validate employee ID
        if (employeeId == null) {
            log.warn("Attempt to fetch unread notifications with null employeeId");
            return List.of();
        }

        log.debug("Fetching unread notifications for employee: {}", employeeId);
        return notificationRepository.findByReceiverIdAndReadFalse(employeeId)
                .stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public List<NotificationResponseDto> getNotificationsForUser(UUID employeeId) {
        // ISSUE FIXED: Validate employee ID
        if (employeeId == null) {
            log.warn("Attempt to fetch notifications with null employeeId");
            throw new ResourceNotFoundException("Employee ID cannot be null");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.warn("Employee not found: {}", employeeId);
                    return new ResourceNotFoundException("Employee with ID: " + employeeId + " not found");
                });

        log.debug("Fetching all notifications for employee: {}", employee.getEmail());
        return notificationRepository.findByReceiver(employee)
                .stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public NotificationResponseDto markAsRead(UUID id) {

        if (id == null) {
            log.warn("Attempt to mark notification as read with null ID");
            throw new ResourceNotFoundException("Notification ID cannot be null");
        }

        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Notification not found: {}", id);
                    return new ResourceNotFoundException("Notification with ID: " + id + " not found");
                });

        n.setRead(true);
        Notification updated = notificationRepository.save(n);
        
        log.debug("Notification marked as read: {}", id);
        return NotificationResponseDto.fromEntity(updated);
    }


    @Override
    public void deleteNotification(UUID id) {

        if (id == null) {
            log.warn("Attempt to delete notification with null ID");
            throw new ResourceNotFoundException("Notification ID cannot be null");
        }


        if (!notificationRepository.existsById(id)) {
            log.warn("Attempting to delete non-existent notification: {}", id);
            throw new ResourceNotFoundException("Notification with ID: " + id + " not found");
        }

        notificationRepository.deleteById(id);
        log.debug("Notification deleted: {}", id);
    }
}

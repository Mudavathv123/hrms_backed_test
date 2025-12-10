package com.hrms.hrm.service.impl;

import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.Notification;
import com.hrms.hrm.model.Notification.TargetRole;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.NotificationRepository;
import com.hrms.hrm.service.NotificationService;
import com.hrms.hrm.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public NotificationResponseDto sendNotification(NotificationRequestDto req) {

        Employee sender = employeeRepository.findById(req.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Employee receiver = null;

        if (req.getReceiverId() != null) {
            receiver = employeeRepository.findById(req.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
        }

        Notification notification = Notification.builder()
                .title(req.getTitle())
                .message(req.getMessage())
                .type(Notification.NotificationType.valueOf(req.getType()))
                .date(LocalDate.now())
                .read(false)
                .sender(sender)
                .receiver(receiver)
                .targetRole(req.getTargetRole() != null ?
                        TargetRole.valueOf(req.getTargetRole()) : null)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponseDto dto = DtoMapper.toDto(saved);

        // 🔥 Push notification to specific user
        if (receiver != null) {
            messagingTemplate.convertAndSend(
                    "/queue/notifications/" + receiver.getId(),
                    dto
            );
        }

        // 🔥 Broadcast to role-based channels
        if (receiver == null && req.getTargetRole() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + req.getTargetRole(),
                    dto
            );
        }

        return dto;
    }

    @Override
    public List<NotificationResponseDto> getUnreadNotifications(UUID employeeId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdAndReadFalse(employeeId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDto> getNotificationsForUser(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return notificationRepository.findByReceiver(employee)
                .stream().map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDto markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        return DtoMapper.toDto(notificationRepository.save(notification));
    }

    @Override
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }

    private NotificationResponseDto convertToDto(Notification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .date(n.getDate())
                .read(n.isRead())
                .senderId(n.getSender().getId())
                .senderName(n.getSender().getFirstName() + " " + n.getSender().getLastName())
                .receiverId(n.getReceiver() != null ? n.getReceiver().getId() : null)
                .receiverName(n.getReceiver() != null ? n.getReceiver().getFirstName() + " " + n.getReceiver().getLastName() : null)
                .targetRole(n.getTargetRole() != null ? n.getTargetRole().name() : null)
                .build();
    }

        public NotificationResponseDto createNotification(NotificationRequestDto req) {

            Employee sender = null;
            Employee receiver = null;
            log.warn("Sender id not found in the createnotifications: {} - Attempt {}/{}", req.getSenderId() );

            // If senderId is present → fetch employee
            if (req.getSenderId() != null) {
                sender = employeeRepository.findById(req.getSenderId())
                        .orElse(null);
            }

            // If receiverId is present → fetch employee
            if (req.getReceiverId() != null) {
                receiver = employeeRepository.findById(req.getReceiverId())
                        .orElse(null);
            }

            Notification notification = Notification.builder()
                    .title(req.getTitle())
                    .message(req.getMessage())
                    .type(Notification.NotificationType.valueOf(req.getType()))
                    .date(req.getDate() != null ? req.getDate() : LocalDate.now())
                    .read(false)
                    .sender(sender)
                    .receiver(receiver)
                    .targetRole(
                            req.getTargetRole() != null ?
                                    Notification.TargetRole.valueOf(req.getTargetRole()) :
                                    null
                    )
                    .build();

            Notification saved = notificationRepository.save(notification);

            // 🔥 WebSocket Push (if you want real-time notifications)
            if (receiver != null) {
                messagingTemplate.convertAndSend(
                        "/queue/notifications/" + receiver.getId(),
                        NotificationResponseDto.fromEntity(saved)
                );
            }

            return NotificationResponseDto.fromEntity(saved);

    }

}

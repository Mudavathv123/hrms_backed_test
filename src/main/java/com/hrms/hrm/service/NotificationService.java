package com.hrms.hrm.service;

import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponseDto> getUnreadNotifications(UUID employeeId);

    NotificationResponseDto sendNotification(NotificationRequestDto req);

    List<NotificationResponseDto> getNotificationsForUser(UUID employeeId);

    NotificationResponseDto markAsRead(UUID notificationId);

    void deleteNotification(UUID id);

    NotificationResponseDto createNotification(NotificationRequestDto req);
}

package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    /** ----------------------------------------
     * SEND NOTIFICATION
     * ---------------------------------------- */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> send(@RequestBody NotificationRequestDto req) {
        // Set senderId as currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Employee sender = employeeRepository.findByEmail(email);
        if(sender == null) {
            throw  new ResourceNotFoundException("Sender not found");
        }

        req.setSenderId(sender.getId());

        NotificationResponseDto sent = notificationService.sendNotification(req);
        return ResponseEntity.ok(ApiResponse.success(sent, "Notification sent"));
    }

    /** ----------------------------------------
     * GET ALL NOTIFICATIONS FOR CURRENT USER
     * ---------------------------------------- */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUserNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Employee employee = employeeRepository.findByEmail(email);
        if(employee == null) {
            throw  new ResourceNotFoundException("Employee not found");
        }

        List<NotificationResponseDto> notifications =
                notificationService.getNotificationsForUser(employee.getId());

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /** ----------------------------------------
     * GET UNREAD NOTIFICATIONS FOR CURRENT USER
     * ---------------------------------------- */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Employee employee = employeeRepository.findByEmail(email);
        if(employee == null)
                throw new ResourceNotFoundException("Employee not found");

        List<NotificationResponseDto> unread =
                notificationService.getUnreadNotifications(employee.getId());

        return ResponseEntity.ok(ApiResponse.success(unread));
    }

    /** ----------------------------------------
     * MARK AS READ
     * ---------------------------------------- */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(@PathVariable UUID notificationId) {
        NotificationResponseDto dto = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(dto, "Notification marked as read"));
    }

    /** ----------------------------------------
     * DELETE NOTIFICATION
     * ---------------------------------------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }
}

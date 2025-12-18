package com.hrms.hrm.controller;

import com.hrms.hrm.config.ApiResponse;
import com.hrms.hrm.dto.NotificationRequestDto;
import com.hrms.hrm.dto.NotificationResponseDto;
import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.model.User;
import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.repository.UserRepository;
import com.hrms.hrm.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> send(@Valid @RequestBody NotificationRequestDto req,
                                                                     Authentication authentication) {
        try {
            log.info("Sending notification - Title: {}, ReceiverID: {}", req.getTitle(), req.getReceiverId());

            if (authentication == null) {
                log.warn("Notification send attempt without authentication");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required", 401));
            }


            String email = authentication.getName();
            if (email == null || email.isEmpty()) {
                log.warn("Authentication principal has no email");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication email not found", 401));
            }


            Employee sender = employeeRepository.findByEmail(email);
            if (sender == null) {
                log.warn("Sender employee not found for email: {}", email);
                throw new ResourceNotFoundException("Sender not found for email: " + email);
            }


            req.setSenderId(sender.getId());

            NotificationResponseDto sent = notificationService.sendNotification(req);
            log.info("Notification sent successfully - ID: {}", sent.getId());
            
            return ResponseEntity.ok(ApiResponse.success(sent, "Notification sent successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while sending notification: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument while sending notification: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Unexpected error while sending notification: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to send notification: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUserNotifications(Authentication authentication) {
        try {
            log.info("Fetching notifications for authenticated user");


            if (authentication == null) {
                log.warn("User notifications request without authentication");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required", 401));
            }

            String email = authentication.getName();
            

            if (email == null || email.isEmpty()) {
                log.warn("Authentication principal has no email");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication email not found", 401));
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getEmployee() == null) {
                return ResponseEntity.ok(
                        ApiResponse.success(List.of(), "No notifications")
                );
            }

            UUID employeeId = user.getEmployee().getId();


            List<NotificationResponseDto> notifications = notificationService.getNotificationsForUser(employeeId);
            log.info("Retrieved {} notifications for user: {}", notifications.size(), email);
            
            return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching user notifications: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve notifications: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getUnreadNotifications(Authentication authentication) {
        try {
            log.info("Fetching unread notifications for authenticated user");

            if (authentication == null) {
                log.warn("Unread notifications request without authentication");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication required", 401));
            }

            String email = authentication.getName();
            

            if (email == null || email.isEmpty()) {
                log.warn("Authentication principal has no email");
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Authentication email not found", 401));
            }

            Employee employee = employeeRepository.findByEmail(email);
            if (employee == null) {
                log.warn("Employee not found for email: {}", email);
                throw new ResourceNotFoundException("Employee not found for email: " + email);
            }

            List<NotificationResponseDto> unread = notificationService.getUnreadNotifications(employee.getId());
            log.info("Retrieved {} unread notifications for user: {}", unread.size(), email);
            
            return ResponseEntity.ok(ApiResponse.success(unread, "Unread notifications retrieved successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching unread notifications: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve unread notifications: " + e.getMessage(), 500));
        }
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponseDto>> markAsRead(@PathVariable UUID notificationId) {
        try {

            if (notificationId == null) {
                log.warn("Mark as read request with null notification ID");
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Notification ID cannot be null", 400));
            }

            log.info("Marking notification as read - ID: {}", notificationId);

            NotificationResponseDto dto = notificationService.markAsRead(notificationId);
            
            return ResponseEntity.ok(ApiResponse.success(dto, "Notification marked as read successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Notification not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error marking notification as read: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to mark notification as read: " + e.getMessage(), 500));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        try {

            if (id == null) {
                log.warn("Delete notification request with null ID");
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Notification ID cannot be null", 400));
            }

            log.info("Deleting notification - ID: {}", id);

            notificationService.deleteNotification(id);
            
            return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("Notification not found: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting notification: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete notification: " + e.getMessage(), 500));
        }
    }
}

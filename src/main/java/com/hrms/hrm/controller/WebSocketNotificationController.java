package com.hrms.hrm.controller;

import com.hrms.hrm.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notify")
    public void notifyUser(NotificationResponseDto dto) {
        // Not used directly but needed for STOMP compliance
        messagingTemplate.convertAndSend("/queue/notifications/" + dto.getReceiverId(), dto);
    }
}

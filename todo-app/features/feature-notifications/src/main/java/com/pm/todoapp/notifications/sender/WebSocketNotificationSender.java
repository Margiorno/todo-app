package com.pm.todoapp.notifications.sender;

import com.pm.todoapp.notifications.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class WebSocketNotificationSender implements NotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(NotificationDTO notification, UUID receiverId) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/notification",
                notification
        );
    }
}

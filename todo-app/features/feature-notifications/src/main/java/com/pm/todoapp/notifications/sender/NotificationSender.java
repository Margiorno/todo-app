package com.pm.todoapp.notifications.sender;

import com.pm.todoapp.notifications.dto.NotificationDTO;

import java.util.UUID;

public interface NotificationSender {
    void send(NotificationDTO notification, UUID receiverId);
}

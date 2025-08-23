package com.pm.todoapp.notifications.mapper;

import com.pm.todoapp.notifications.dto.FriendRequestUserDTO;
import com.pm.todoapp.notifications.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.model.Notification;

import java.time.format.DateTimeFormatter;


public class NotificationMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm");

    public static NotificationDTO toDTO(Notification notification) {

        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .notificationTime(notification.getCreatedAt().format(FORMATTER))
                .isRead(notification.isRead())
                .build();
    }

    public static NotificationDTO toDTO(FriendRequestNotification frn, FriendRequestUserDTO userDTO) {
        return FriendRequestNotificationDTO.builder()
                .id(frn.getId())
                .message(frn.getMessage())
                .type(frn.getType())
                .notificationTime(frn.getCreatedAt().format(FORMATTER))
                .isRead(frn.isRead())
                .sender(userDTO)
                .resolved(frn.isResolved())
                .requestId(frn.getRequestId())
                .build();
    }
}

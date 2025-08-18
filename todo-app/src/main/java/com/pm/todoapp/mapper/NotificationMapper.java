package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.dto.NotificationDTO;
import com.pm.todoapp.model.FriendRequestNotification;
import com.pm.todoapp.model.Notification;

import java.time.format.DateTimeFormatter;


public class NotificationMapper {
    public static NotificationDTO toDTO(Notification notification) {
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm");

        if (notification instanceof FriendRequestNotification frn) {
            return FriendRequestNotificationDTO.builder()
                    .id(frn.getId())
                    .message(frn.getMessage())
                    .type(frn.getType())
                    .notificationTime(frn.getCreatedAt().format(FORMATTER))
                    .isRead(frn.isRead())
                    .sender(UserMapper.toUserResponseDTO(frn.getSender()))
                    .resolved(frn.isResolved())
                    .requestId(frn.getRequestId())
                    .build();
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .notificationTime(notification.getCreatedAt().format(FORMATTER))
                .isRead(notification.isRead())
                .build();
    }
}

package com.pm.todoapp.notifications.mapper;

import com.pm.todoapp.users.profile.mapper.UserMapper;
import com.pm.todoapp.users.social.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.model.Notification;

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

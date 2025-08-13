package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.model.FriendRequest;
import com.pm.todoapp.model.FriendRequestNotification;
import com.pm.todoapp.model.Notification;
import com.pm.todoapp.model.NotificationType;


public class NotificationMapper {
    public static FriendRequestNotificationDTO friendRequestNotificationDTO(FriendRequestNotification notification) {

        return FriendRequestNotificationDTO.builder()
                .id(notification.getId())
                .type(NotificationType.FRIEND_REQUEST)
                .message(notification.getMessage())
                .senderId(notification.getSender().getId())
                .build();
    }

}

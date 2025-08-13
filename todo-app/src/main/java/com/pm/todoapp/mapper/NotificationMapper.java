package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.NotificationDTO;
import com.pm.todoapp.model.Notification;


public class NotificationMapper {
    public static NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .build();
    }
}

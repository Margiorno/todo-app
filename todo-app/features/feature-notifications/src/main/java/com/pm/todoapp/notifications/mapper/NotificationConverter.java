package com.pm.todoapp.notifications.mapper;

import com.pm.todoapp.core.user.dto.UserDTO;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.notifications.dto.FriendRequestUserDTO;
import com.pm.todoapp.notifications.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class NotificationConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm");
    private final UserProviderPort userProviderPort;
    private final UserValidationPort userValidationPort;


    public NotificationDTO toDTO(Notification notification) {
        if (notification instanceof FriendRequestNotification frn) {
            UUID senderId = frn.getSender().getId();
            userValidationPort.ensureUserExistsById(senderId);
            UserDTO senderData = userProviderPort.getUserById(senderId);

            FriendRequestUserDTO senderDTO = FriendRequestUserDTO.builder()
                    .id(senderData.getId().toString())
                    .profilePicturePath(senderData.getProfilePicturePath())
                    .firstName(senderData.getFirstName())
                    .lastName(senderData.getLastName())
                    .build();

            return mapToDTO(frn, senderDTO);
        } else {
            return mapToDTO(notification);
        }
    }

    public static NotificationDTO mapToDTO(Notification notification) {

        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .notificationTime(notification.getCreatedAt().format(FORMATTER))
                .isRead(notification.isRead())
                .build();
    }

    public static NotificationDTO mapToDTO(FriendRequestNotification frn, FriendRequestUserDTO userDTO) {
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

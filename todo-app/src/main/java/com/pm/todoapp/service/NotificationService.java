package com.pm.todoapp.service;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.FriendRequestNotificationDTO;
import com.pm.todoapp.dto.NotificationDTO;
import com.pm.todoapp.mapper.NotificationMapper;
import com.pm.todoapp.model.*;
import com.pm.todoapp.repository.NotificationRepository;
import org.hibernate.internal.build.AllowNonPortable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UsersService usersService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UsersService usersService, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.usersService = usersService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationDTO createNotification(FriendRequestDTO invitation) {

        User sender = usersService.findRawById(invitation.getSenderId());
        User receiver = usersService.findRawById(invitation.getReceiverId());
        String message = "Friend request from: %s %s".formatted(sender.getFirstName(), sender.getLastName());

        FriendRequestNotification notificationEntity = FriendRequestNotification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(NotificationType.FRIEND_REQUEST)
                .message(message)
                .build();
        notificationRepository.save(notificationEntity);

        FriendRequestNotification saved = notificationRepository.save(notificationEntity);

        return FriendRequestNotificationDTO.builder()
                .id(invitation.getId())
                .type(NotificationType.FRIEND_REQUEST)
                .message(message)
                .senderId(sender.getId())
                .build();
    }

    public NotificationDTO createNotification(User receiver, User currentUser, NotificationType notificationType, String notificationMessage) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(notificationType)
                .message(notificationMessage)
                .build();

        Notification saved = notificationRepository.save(notification);

        return NotificationMapper.toDTO(saved);
    }


    public void sendNotification(NotificationDTO notification, UUID receiverId) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/notification",
                notification
        );
    }
}

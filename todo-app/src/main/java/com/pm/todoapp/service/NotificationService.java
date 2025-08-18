package com.pm.todoapp.service;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.NotificationDTO;
import com.pm.todoapp.exceptions.NotificationNotFoundException;
import com.pm.todoapp.exceptions.UnauthorizedException;
import com.pm.todoapp.mapper.NotificationMapper;
import com.pm.todoapp.model.*;
import com.pm.todoapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

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

    public Notification findRawById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    public FriendRequestNotification findRawFriendRequestNotificationById(UUID id) {
        return notificationRepository.findFriendRequestNotificationById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    @Transactional
    public NotificationDTO createNotification(FriendRequestDTO invitation) {

        User sender = usersService.findRawById(invitation.getSenderId());
        User receiver = usersService.findRawById(invitation.getReceiverId());
        String message = "has sent you a friend request";

        FriendRequestNotification notificationEntity = FriendRequestNotification.builder()
                .requestId(invitation.getId())
                .receiver(receiver)
                .sender(sender)
                .type(NotificationType.FRIEND_REQUEST)
                .message(message)
                .build();

        FriendRequestNotification saved = notificationRepository.save(notificationEntity);

        return NotificationMapper.toDTO(saved);
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

    public List<NotificationDTO> getAllNotificationsByUserId(UUID userId) {

        User user = usersService.findRawById(userId);
        Iterable<Notification> notifications = notificationRepository.findAllByReceiver(user);

        return StreamSupport.stream(notifications.spliterator(), false)
                .sorted(Comparator.comparing(Notification::getCreatedAt))
                .map(NotificationMapper::toDTO)
                .toList();
    }

    public void readNotification(UUID id, UUID userId) {
        Notification notification = findRawById(id);

        if (!notification.getReceiver().getId().equals(userId))
            throw new UnauthorizedException("You are not authorized to read this notification");

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void resolveNotification(UUID requestId) {
        FriendRequestNotification friendRequestNotification = getFriendRequestNotification(requestId);

        friendRequestNotification.setResolved(true);
        notificationRepository.save(friendRequestNotification);
    }

    public void deleteNotification(UUID requestId) {
        FriendRequestNotification friendRequestNotification = getFriendRequestNotification(requestId);
        notificationRepository.delete(friendRequestNotification);
    }

    private FriendRequestNotification getFriendRequestNotification(UUID requestId) {
        return notificationRepository
                .findNotificationByRequestId(requestId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
    }

    @Transactional
    public void markAllReadByUserId(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }
}

package com.pm.todoapp.service;

import com.pm.todoapp.dto.FriendRequestDTO;
import com.pm.todoapp.dto.NotificationDTO;
import com.pm.todoapp.mapper.NotificationMapper;
import com.pm.todoapp.model.*;
import com.pm.todoapp.repository.NotificationRepository;
import org.hibernate.internal.build.AllowNonPortable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UsersService usersService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UsersService usersService) {
        this.notificationRepository = notificationRepository;
        this.usersService = usersService;
    }

    @Transactional
    public NotificationDTO createNotification(FriendRequestDTO invitation) {

        User sender = usersService.findRawById(invitation.getSenderId());
        User receiver = usersService.findRawById(invitation.getReceiverId());

        FriendRequestNotification notification = FriendRequestNotification.builder()
                .receiver(receiver)
                .sender(sender)
                .status(FriendRequestStatus.PENDING)
                .type(NotificationType.FRIEND_REQUEST)
                .message("Friend request from: %s %s".formatted(sender.getFirstName(), sender.getLastName()))
                .build();

        FriendRequestNotification saved = notificationRepository.save(notification);

        return NotificationMapper.friendRequestNotificationDTO(saved);
    }
}

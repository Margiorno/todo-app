package com.pm.todoapp.notifications.service;

import com.pm.todoapp.core.user.dto.UserDTO;
import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.notifications.dto.FriendRequestUserDTO;
import com.pm.todoapp.notifications.factory.NotificationFactory;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.core.exceptions.NotificationNotFoundException;
import com.pm.todoapp.notifications.mapper.NotificationConverter;
import com.pm.todoapp.notifications.model.Notification;
import com.pm.todoapp.notifications.repository.NotificationRepository;
import com.pm.todoapp.notifications.sender.NotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserValidationPort userValidationPort;
    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationSender notificationSender;
    private final NotificationConverter notificationConverter;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               UserValidationPort userValidationPort,
                               UserRepository userRepository,
                               NotificationFactory notificationFactory,
                               NotificationSender notificationSender,
                               NotificationConverter notificationConverter) {

        this.notificationRepository = notificationRepository;
        this.userValidationPort = userValidationPort;
        this.userRepository = userRepository;
        this.notificationFactory = notificationFactory;
        this.notificationSender = notificationSender;
        this.notificationConverter = notificationConverter;
    }

    public List<NotificationDTO> getAllNotificationsByUserId(UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Notification> notifications = notificationRepository.findAllByReceiver(user);

        return StreamSupport.stream(notifications.spliterator(), false)
                .sorted(Comparator.comparing(Notification::getCreatedAt))
                .map(notificationConverter::toDTO)
                .toList();
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAndSendFriendRequestNotification(UUID requestId, UUID senderId, UUID receiverId) {
        userValidationPort.ensureUserExistsById(senderId);
        userValidationPort.ensureUserExistsById(receiverId);

        User sender = userRepository.getReferenceById(senderId);
        User receiver = userRepository.getReferenceById(receiverId);

        FriendRequestNotification notificationEntity = notificationFactory
                .createFriendRequestNotification(requestId, sender, receiver);

        FriendRequestNotification saved = notificationRepository.save(notificationEntity);

        NotificationDTO notificationDTO = notificationConverter.toDTO(saved);
        notificationSender.send(notificationDTO, receiverId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFriendRequestAcceptedNotification(UUID acceptorId, UUID senderId) {

        userValidationPort.ensureUserExistsById(senderId);
        userValidationPort.ensureUserExistsById(acceptorId);

        User sender = userRepository.getReferenceById(senderId);
        User acceptor = userRepository.getReferenceById(acceptorId);

        Notification notificationEntity = notificationFactory
                .createFriendRequestAcceptedNotification(acceptor, sender);

        Notification saved = notificationRepository.save(notificationEntity);
        NotificationDTO notificationDTO = notificationConverter.toDTO(saved);

        notificationSender.send(notificationDTO, senderId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolveNotification(UUID requestId) {
        FriendRequestNotification friendRequestNotification = getFriendRequestNotification(requestId);

        friendRequestNotification.setResolved(true);
        notificationRepository.save(friendRequestNotification);
    }
}

package com.pm.todoapp.notifications.service;

import com.pm.todoapp.core.user.dto.UserDTO;
import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.notifications.dto.FriendRequestUserDTO;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.core.exceptions.NotificationNotFoundException;
import com.pm.todoapp.notifications.mapper.NotificationMapper;
import com.pm.todoapp.notifications.model.Notification;
import com.pm.todoapp.notifications.model.NotificationType;
import com.pm.todoapp.notifications.repository.NotificationRepository;
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
    private final SimpMessagingTemplate messagingTemplate;
    private final UserValidationPort userValidationPort;
    private final UserProviderPort userProviderPort;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate, UserValidationPort userValidationPort, UserProviderPort userProviderPort, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userValidationPort = userValidationPort;
        this.userProviderPort = userProviderPort;
        this.userRepository = userRepository;
    }

    public void sendNotification(NotificationDTO notification, UUID receiverId) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/notification",
                notification
        );
    }

    public List<NotificationDTO> getAllNotificationsByUserId(UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Notification> notifications = notificationRepository.findAllByReceiver(user);

        return StreamSupport.stream(notifications.spliterator(), false)
                .sorted(Comparator.comparing(Notification::getCreatedAt))
                .map(this::mapNotificationToDto)
                .toList();
    }

    private NotificationDTO mapNotificationToDto(Notification notification) {
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

            return NotificationMapper.toDTO(frn, senderDTO);
        } else {
            return NotificationMapper.toDTO(notification);
        }
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

        UserDTO senderData = userProviderPort.getUserById(senderId);
        String message = "%s %s has sent you a friend request".formatted(senderData.getFirstName(), senderData.getLastName());

        FriendRequestNotification notificationEntity = FriendRequestNotification.builder()
                .requestId(requestId)
                .receiver(receiver)
                .sender(sender)
                .type(NotificationType.FRIEND_REQUEST)
                .message(message)
                .build();

        FriendRequestNotification saved = notificationRepository.save(notificationEntity);
        FriendRequestUserDTO senderDTO = FriendRequestUserDTO.builder()
                .id(senderData.getId().toString())
                .profilePicturePath(senderData.getProfilePicturePath())
                .firstName(senderData.getFirstName())
                .lastName(senderData.getLastName())
                .build();



        NotificationDTO notificationDTO = NotificationMapper.toDTO(saved, senderDTO);
        sendNotification(notificationDTO, receiverId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFriendRequestAcceptedNotification(UUID acceptorId, UUID senderId) {

        userValidationPort.ensureUserExistsById(senderId);
        userValidationPort.ensureUserExistsById(acceptorId);

        User sender = userRepository.getReferenceById(senderId);
        UserDTO acceptorData = userProviderPort.getUserById(acceptorId);
        String message = "%s %s accepted your friend request".formatted(acceptorData.getFirstName(), acceptorData.getLastName());

        Notification notification = Notification.builder()
                .receiver(sender)
                .message(message)
                .type(NotificationType.FRIEND_REQUEST_ACCEPTED)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDTO notificationDTO = NotificationMapper.toDTO(saved);

        sendNotification(notificationDTO, senderId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolveNotification(UUID requestId) {
        FriendRequestNotification friendRequestNotification = getFriendRequestNotification(requestId);

        friendRequestNotification.setResolved(true);
        notificationRepository.save(friendRequestNotification);
    }
}

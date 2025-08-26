package com.pm.todoapp.notifications.factory;

import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.notifications.model.FriendRequestNotification;
import com.pm.todoapp.domain.notifications.model.Notification;
import com.pm.todoapp.domain.notifications.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class NotificationFactory {

    private final UserProviderPort userProviderPort;

    public FriendRequestNotification createFriendRequestNotification(UUID requestId, User sender, User receiver) {
        UserDTO senderData = userProviderPort.getUserById(sender.getId());
        String message = String.format("%s %s has sent you a friend request", senderData.getFirstName(), senderData.getLastName());

        return FriendRequestNotification.builder()
                .requestId(requestId)
                .receiver(receiver)
                .sender(sender)
                .type(NotificationType.FRIEND_REQUEST)
                .message(message)
                .build();
    }


    public Notification createFriendRequestAcceptedNotification(User acceptor, User originalSender) {
        UserDTO acceptorData = userProviderPort.getUserById(acceptor.getId());
        String message = String.format("%s %s accepted your friend request", acceptorData.getFirstName(), acceptorData.getLastName());

        return Notification.builder()
                .receiver(originalSender)
                .message(message)
                .type(NotificationType.FRIEND_REQUEST_ACCEPTED)
                .build();
    }
}

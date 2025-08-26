package com.pm.todoapp.notifications.listener;

import com.pm.todoapp.domain.user.event.FriendRequestAcceptedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestResolvedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestSentEvent;
import com.pm.todoapp.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class UserNotificationsListener {
    private final NotificationService notificationService;

    @TransactionalEventListener
    public void handleFriendRequestSent(FriendRequestSentEvent event) {
        notificationService.createAndSendFriendRequestNotification(
                event.requestId(), event.senderId(), event.receiverId()
        );
    }

    @TransactionalEventListener
    public void handleFriendRequestAccepted(FriendRequestAcceptedEvent event) {
        notificationService.createFriendRequestAcceptedNotification(
                event.acceptorId(), event.senderId()
        );
    }

    @TransactionalEventListener
    public void handleFriendRequestResolved(FriendRequestResolvedEvent event) {
        notificationService.resolveNotification(
                event.requestId()
        );
    }
}

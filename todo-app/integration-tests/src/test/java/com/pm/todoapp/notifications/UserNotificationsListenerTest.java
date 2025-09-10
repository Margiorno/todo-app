package com.pm.todoapp.notifications;

import com.pm.todoapp.domain.user.event.FriendRequestAcceptedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestResolvedEvent;
import com.pm.todoapp.domain.user.event.FriendRequestSentEvent;
import com.pm.todoapp.notifications.service.NotificationService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.*;

@SpringBootTest
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // There is no error – IntelliJ just underlines it incorrectly
public class UserNotificationsListenerTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void shouldHandleFriendRequestSentEvent() {
        FriendRequestSentEvent event = Instancio.create(FriendRequestSentEvent.class);

        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(event);
            return null;
        });

        verify(notificationService, times(1)).createAndSendFriendRequestNotification(
                event.requestId(),
                event.senderId(),
                event.receiverId()
        );
    }

    @Test
    public void shouldHandleFriendRequestAcceptedEvent() {
        FriendRequestAcceptedEvent event = Instancio.create(FriendRequestAcceptedEvent.class);

        transactionTemplate.execute(status->{
            eventPublisher.publishEvent(event);
            return null;
        });

        verify(notificationService, times(1)).createFriendRequestAcceptedNotification(
                event.acceptorId(), event.senderId()
        );
    }

    @Test
    public void shouldHandleFriendRequestResolvedEvent() {
        FriendRequestResolvedEvent event = Instancio.create(FriendRequestResolvedEvent.class);

        transactionTemplate.execute(status->{
            eventPublisher.publishEvent(event);
            return null;
        });

        verify(notificationService, times(1)).resolveNotification(event.requestId());
    }
}

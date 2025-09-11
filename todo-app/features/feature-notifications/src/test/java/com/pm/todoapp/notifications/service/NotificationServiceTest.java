package com.pm.todoapp.notifications.service;

import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.factory.NotificationFactory;
import com.pm.todoapp.notifications.mapper.NotificationConverter;
import com.pm.todoapp.notifications.model.FriendRequestNotification;
import com.pm.todoapp.notifications.model.Notification;
import com.pm.todoapp.notifications.repository.NotificationRepository;
import com.pm.todoapp.notifications.sender.NotificationSender;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserValidationPort userValidationPort;
    @Mock private NotificationConverter notificationConverter;
    @Mock private NotificationSender notificationSender;
    @Mock private NotificationFactory notificationFactory;

    @InjectMocks
    private NotificationService notificationService;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        UUID user1Id = UUID.randomUUID();
        user1 = User.builder().id(user1Id).build();

        UUID user2Id = UUID.randomUUID();
        user2 = User.builder().id(user2Id).build();
    }


    @Test
    public void findAllNotificationsByUser_shouldReturnNotificationsInRightOrder(){

        Notification todayNotification = Instancio.of(Notification.class)
                .set(field(Notification::getReceiver),user1)
                .set(field(Notification::getCreatedAt), LocalDateTime.now())
                .create();

        Notification yesterdayNotification = Instancio.of(Notification.class)
                .set(field(Notification::getReceiver),user1)
                .set(field(Notification::getCreatedAt), LocalDateTime.now().minusDays(1))
                .create();

        NotificationDTO todayDto = new NotificationDTO();
        todayDto.setId(todayNotification.getId());

        NotificationDTO yesterdayDto = new NotificationDTO();
        yesterdayDto.setId(yesterdayNotification.getId());

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1.getId())).thenReturn(user1);
        when(notificationRepository.findAllByReceiver(user1)).thenReturn(Set.of(todayNotification, yesterdayNotification));

        when(notificationConverter.toDTO(todayNotification)).thenReturn(todayDto);
        when(notificationConverter.toDTO(yesterdayNotification)).thenReturn(yesterdayDto);

        List<NotificationDTO> result = notificationService.getAllNotificationsByUserId(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(todayNotification.getId());
        assertThat(result.getLast().getId()).isEqualTo(yesterdayNotification.getId());
    }

    @Test
    public void createAndSendFriendRequestNotification_shouldSendNotification() {
        UUID requestId = UUID.randomUUID();

        FriendRequestNotification notificationEntity = Instancio.create(FriendRequestNotification.class);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .id(notificationEntity.getId())
                .build();

        doNothing().when(userValidationPort).ensureUserExistsById(any(UUID.class));
        when(userRepository.getReferenceById(user1.getId())).thenReturn(user1);
        when(userRepository.getReferenceById(user2.getId())).thenReturn(user2);
        when(notificationFactory.createFriendRequestNotification(requestId, user1, user2))
                .thenReturn(notificationEntity);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationEntity);
        when(notificationConverter.toDTO(notificationEntity)).thenReturn(notificationDTO);
        doNothing().when(notificationSender).send(notificationDTO, user2.getId());

        notificationService.createAndSendFriendRequestNotification(requestId, user1.getId(), user2.getId());

        verify(userValidationPort, times(1)).ensureUserExistsById(user1.getId());
        verify(userValidationPort, times(1)).ensureUserExistsById(user2.getId());
        verify(notificationFactory, times(1)).createFriendRequestNotification(requestId, user1, user2);
        verify(notificationRepository, times(1)).save(notificationEntity);
        verify(notificationConverter, times(1)).toDTO(notificationEntity);
        verify(notificationSender, times(1)).send(notificationDTO, user2.getId());
    }

    @Test
    void resolveFriendRequestNotification_shouldResolveNotification() {
        UUID requestId = UUID.randomUUID();
        FriendRequestNotification notification = Instancio.of(FriendRequestNotification.class)
                .set(field(FriendRequestNotification::getRequestId),requestId)
                .set(field(FriendRequestNotification::isResolved),false)
                .create();

        when(notificationRepository.findNotificationByRequestId(requestId)).thenReturn(Optional.ofNullable(notification));

        ArgumentCaptor<FriendRequestNotification> notificationCaptor =
                ArgumentCaptor.forClass(FriendRequestNotification.class);

        notificationService.resolveNotification(requestId);

        verify(notificationRepository, times(1)).findNotificationByRequestId(requestId);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        FriendRequestNotification capturedNotification = notificationCaptor.getValue();
        assertThat(capturedNotification.isResolved()).isTrue();
    }
}

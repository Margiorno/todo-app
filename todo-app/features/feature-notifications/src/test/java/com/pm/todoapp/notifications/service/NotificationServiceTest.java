package com.pm.todoapp.notifications.service;

import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.notifications.mapper.NotificationConverter;
import com.pm.todoapp.notifications.model.Notification;
import com.pm.todoapp.notifications.model.NotificationType;
import com.pm.todoapp.notifications.repository.NotificationRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserValidationPort userValidationPort;
    @Mock private NotificationConverter notificationConverter;

    @InjectMocks
    private NotificationService notificationService;

    private User user1;

    @BeforeEach
    public void setUp() {
        UUID user1Id = UUID.randomUUID();
        user1 = User.builder().id(user1Id).build();
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

}

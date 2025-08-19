package com.pm.todoapp.users.social.dto;

import com.pm.todoapp.notifications.dto.NotificationDTO;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FriendRequestNotificationDTO extends NotificationDTO {
    private UserResponseDTO sender;
    private boolean resolved;
    private UUID requestId;
}

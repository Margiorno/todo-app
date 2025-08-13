package com.pm.todoapp.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FriendRequestNotificationDTO extends NotificationDTO {
    private UUID senderId;
}

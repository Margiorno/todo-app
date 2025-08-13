package com.pm.todoapp.dto;

import com.pm.todoapp.model.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDTO {
    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private FriendRequestStatus status;
}

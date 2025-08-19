package com.pm.todoapp.chat.dto;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private UUID conversationId;
    private UserResponseDTO sender;
    private String content;
    private LocalDateTime sendAt;
    private boolean sentByCurrentUser;
}

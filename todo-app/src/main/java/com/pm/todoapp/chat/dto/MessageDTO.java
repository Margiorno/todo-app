package com.pm.todoapp.chat.dto;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageDTO {
    private String content;
    private UserResponseDTO sender;
    private UUID conversationId;
}

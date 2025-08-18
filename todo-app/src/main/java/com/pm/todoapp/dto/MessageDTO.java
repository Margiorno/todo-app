package com.pm.todoapp.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageDTO {
    private String content;
    private UserResponseDTO sender;
    private UUID conversationId;
}

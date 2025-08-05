package com.pm.todoapp.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageDTO {
    private String content;
    private UUID senderId;
    private UUID conversationId;
}

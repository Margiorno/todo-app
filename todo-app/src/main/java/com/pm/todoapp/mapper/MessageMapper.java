package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.model.Message;

import java.util.UUID;

public class MessageMapper {
    public static MessageResponseDTO toResponseDTO(Message message, UUID userId) {
        return MessageResponseDTO.builder()
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .context(message.getContent())
                .sendAt(message.getSentAt())
                .sentByCurrentUser(message.getSender().getId().equals(userId))
                .build();
    }
}

package com.pm.todoapp.chat.mapper;

import com.pm.todoapp.chat.dto.MessageResponseDTO;
import com.pm.todoapp.chat.dto.SenderDTO;
import com.pm.todoapp.chat.model.Message;

import java.util.UUID;

public class MessageMapper {
    public static MessageResponseDTO toResponseDTO(Message message, SenderDTO senderDTO, UUID currentUserId) {
        return MessageResponseDTO.builder()
                .conversationId(message.getConversation().getId())
                .sender(senderDTO)
                .content(message.getContent())
                .sendAt(message.getSentAt())
                .sentByCurrentUser(senderDTO.getId().equals(currentUserId.toString()))
                .build();
    }
}

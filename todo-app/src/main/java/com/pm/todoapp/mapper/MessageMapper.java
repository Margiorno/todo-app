package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.MessageResponseDTO;
import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.model.Message;

import java.util.UUID;

public class MessageMapper {
    public static MessageResponseDTO toResponseDTO(Message message, UserResponseDTO userResponseDTO) {
        return MessageResponseDTO.builder()
                .conversationId(message.getConversation().getId())
                .sender(UserMapper.toUserResponseDTO(message.getSender()))
                .content(message.getContent())
                .sendAt(message.getSentAt())
                .sentByCurrentUser(message.getSender().getId().equals(UUID.fromString(userResponseDTO.getId())))
                .build();
    }
}

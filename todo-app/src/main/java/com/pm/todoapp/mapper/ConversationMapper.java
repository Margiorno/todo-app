package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.ConversationResponseDTO;
import com.pm.todoapp.model.Conversation;

public class ConversationMapper {
    public static ConversationResponseDTO toResponseDTO(Conversation conversation) {
        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .type(conversation.getConversationType())
                .title(conversation.getTitle())
                .build();
    }
}

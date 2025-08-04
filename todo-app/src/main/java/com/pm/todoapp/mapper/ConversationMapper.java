package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.ConversationResponseDTO;
import com.pm.todoapp.dto.InviteResponseDTO;
import com.pm.todoapp.model.Conversation;
import com.pm.todoapp.model.Invite;

public class ConversationMapper {
    public static ConversationResponseDTO toResponseDTO(Conversation conversation) {
        return ConversationResponseDTO.builder()
                .id(conversation.getId())
                .build();
    }
}

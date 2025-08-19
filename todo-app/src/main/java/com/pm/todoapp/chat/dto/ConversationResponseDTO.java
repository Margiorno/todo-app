package com.pm.todoapp.chat.dto;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.chat.model.ConversationType;
import lombok.*;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDTO {
    private UUID id;
    private ConversationType type;
    private String title;
    private List<UserResponseDTO> participants;
}

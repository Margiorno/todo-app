package com.pm.todoapp.chat.dto;

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
    private Set<SenderDTO> participants;
}

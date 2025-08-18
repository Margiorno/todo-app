package com.pm.todoapp.dto;

import com.pm.todoapp.model.ConversationType;
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

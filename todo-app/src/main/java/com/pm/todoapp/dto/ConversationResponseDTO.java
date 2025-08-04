package com.pm.todoapp.dto;

import com.pm.todoapp.model.Message;
import com.pm.todoapp.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponseDTO {
    private UUID id;
}

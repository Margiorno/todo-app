package com.pm.todoapp.tasks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUserDTO {
    private UUID id;
    private String profilePicturePath;
    private String firstName;
    private String lastName;
}

package com.pm.todoapp.users.profile.dto;

import com.pm.todoapp.model.ProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProfileStatusDTO {
    private ProfileStatus status;
    private UUID friendRequestId;
}

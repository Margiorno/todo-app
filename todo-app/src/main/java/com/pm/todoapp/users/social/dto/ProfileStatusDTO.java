package com.pm.todoapp.users.social.dto;

import com.pm.todoapp.users.profile.dto.ProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ProfileStatusDTO {
    private ProfileStatus status;
    private UUID friendRequestId;
}

package com.pm.todoapp.domain.user.port;


import com.pm.todoapp.domain.user.dto.UserDTO;

import java.util.Set;
import java.util.UUID;

public interface UserProviderPort {
    UserDTO getUserById(UUID userId);

    Set<UserDTO> getUsersByIds(Set<UUID> participantsIds);

    String getUserName(UUID senderId);
}

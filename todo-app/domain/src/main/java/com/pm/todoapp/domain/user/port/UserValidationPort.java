package com.pm.todoapp.domain.user.port;

import java.util.Set;
import java.util.UUID;

public interface UserValidationPort {
    void ensureUserExistsById(UUID id);
    void ensureUsersExistsById(Set<UUID> ids);
}

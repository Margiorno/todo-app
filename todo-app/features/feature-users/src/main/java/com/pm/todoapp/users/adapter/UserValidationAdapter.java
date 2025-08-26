package com.pm.todoapp.users.adapter;

import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.users.profile.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UserValidationAdapter implements UserValidationPort {

    private final UsersService usersService;

    @Override
    public void ensureUserExistsById(UUID id) {
        usersService.ensureUserExistsById(id);
    }

    @Override
    public void ensureUsersExistsById(Set<UUID> ids) {
        ids.forEach(this::ensureUserExistsById);
    }
}

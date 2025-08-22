package com.pm.todoapp.users.adapter;

import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.users.profile.service.UsersService;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class UserValidationAdapter implements UserValidationPort {

    private final UsersService usersService;

    public UserValidationAdapter(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public void ensureUserExistsById(UUID id) {
        usersService.ensureUserExistsById(id);
    }

    @Override
    public void ensureUsersExistsById(Set<UUID> ids) {
        ids.forEach(this::ensureUserExistsById);
    }
}

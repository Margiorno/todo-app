package com.pm.todoapp.service;

import com.pm.todoapp.exceptions.InvalidTokenException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {


    private final UsersService usersService;

    public AuthService(UsersService usersService) {
        this.usersService = usersService;
    }

    // TODO in future userId replaced with JWT, rebuild whole method
    public UUID verifyToken(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new InvalidTokenException("User cookie is missing or empty.");
        }
        UUID uuid = UUID.fromString(userId);

        usersService.findById(uuid);

        return uuid;
    }
}

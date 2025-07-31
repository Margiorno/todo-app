package com.pm.todoapp.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {


    private final UsersService usersService;

    public AuthService(UsersService usersService) {
        this.usersService = usersService;
    }

    // TODO in future userId replaced with token, and rebuild whole method
    public UUID verifyToken(String userId) {
        UUID uuid = UUID.fromString(userId);

        usersService.findById(uuid);

        return uuid;
    }
}

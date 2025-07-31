package com.pm.todoapp.service;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.exceptions.InvalidTokenException;
import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UsersService usersService, PasswordEncoder passwordEncoder) {
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
    }

    // TODO return token
    // TODO hash password
    // TODO rebuild both methods (login register)
    public UUID registerUser(RegisterRequestDTO registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = usersService.save(user);
        return savedUser.getId();
    }

    public UUID loginUser(LoginRequestDTO loginRequestDTO) {
        User user = usersService.findByEmail(loginRequestDTO.getEmail());

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())){
            throw new InvalidTokenException("Wrong password");
        }

        return user.getId();
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

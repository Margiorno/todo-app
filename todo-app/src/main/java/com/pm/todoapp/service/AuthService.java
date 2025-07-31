package com.pm.todoapp.service;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.exceptions.InvalidTokenException;
import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UsersService usersService;
    private final UsersRepository usersRepository;

    public AuthService(UsersService usersService, UsersRepository usersRepository) {
        this.usersService = usersService;
        this.usersRepository = usersRepository;
    }

    // TODO return token
    // TODO hash password
    // TODO rebuild both methods (login register)
    public UUID registerUser(RegisterRequestDTO registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());

        User savedUser = usersRepository.save(user);
        return savedUser.getId();
    }

    public UUID loginUser(LoginRequestDTO loginRequestDTO) {
        User user = usersRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow(
                ()-> new UserNotFoundException("User with this email does not exist: " + loginRequestDTO.getEmail())
        );

        if(!user.getPassword().equals(loginRequestDTO.getPassword())){
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

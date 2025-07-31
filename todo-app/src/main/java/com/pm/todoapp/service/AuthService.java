package com.pm.todoapp.service;

import com.pm.todoapp.dto.LoginRequestDTO;
import com.pm.todoapp.dto.RegisterRequestDTO;
import com.pm.todoapp.exceptions.InvalidTokenException;
import com.pm.todoapp.exceptions.UserNotFoundException;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.UsersRepository;
import com.pm.todoapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UsersService usersService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // TODO return token
    // TODO hash password
    // TODO rebuild both methods (login register)
    public String registerUser(RegisterRequestDTO registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = usersService.save(user);
        return jwtUtil.generateToken(user.getId().toString());
    }

    public String loginUser(LoginRequestDTO loginRequestDTO) {
        User user = usersService.findByEmail(loginRequestDTO.getEmail());

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())){
            throw new InvalidTokenException("Wrong password");
        }

        return jwtUtil.generateToken(user.getId().toString());
    }


}

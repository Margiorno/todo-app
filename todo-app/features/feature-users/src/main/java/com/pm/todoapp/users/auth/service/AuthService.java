package com.pm.todoapp.users.auth.service;

import com.pm.todoapp.common.exceptions.EmailAlreadyExistsException;
import com.pm.todoapp.common.exceptions.UnauthorizedException;
import com.pm.todoapp.common.security.JwtUtil;
import com.pm.todoapp.users.auth.dto.LoginRequestDTO;
import com.pm.todoapp.users.auth.dto.RegisterRequestDTO;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public String registerUser(RegisterRequestDTO registerRequest) {

        if (usersService.existByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Account with this email already exists");
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .gender(registerRequest.getGender())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        User savedUser = usersService.save(user);
        return jwtUtil.generateToken(savedUser.getId().toString());
    }

    public String loginUser(LoginRequestDTO loginRequestDTO) {
        User user = usersService.findByEmail(loginRequestDTO.getEmail());

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())){
            throw new UnauthorizedException("Wrong password");
        }

        return jwtUtil.generateToken(user.getId().toString());
    }


}

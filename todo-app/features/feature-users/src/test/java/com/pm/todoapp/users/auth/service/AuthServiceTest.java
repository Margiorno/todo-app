package com.pm.todoapp.users.auth.service;

import com.pm.todoapp.common.exceptions.EmailAlreadyExistsException;
import com.pm.todoapp.common.security.JwtUtil;
import com.pm.todoapp.users.auth.dto.RegisterRequestDTO;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.service.UsersService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsersService usersService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_shouldSaveUserAndReturnToken_whenEmailIsUnique() {
        RegisterRequestDTO registerRequest = Instancio.create(RegisterRequestDTO.class);
        String hashedPassword = Instancio.create(String.class);
        String generatedToken = Instancio.create(String.class);
        User savedUser = Instancio.create(User.class);

        when(usersService.existByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(hashedPassword);
        when(usersService.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser.getId().toString())).thenReturn(generatedToken);

        String resultToken = authService.registerUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersService).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(capturedUser.getFirstName()).isEqualTo(registerRequest.getFirstName());
        assertThat(capturedUser.getPassword()).isEqualTo(hashedPassword);

        verify(usersService).existByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(jwtUtil).generateToken(savedUser.getId().toString());

        assertThat(resultToken).isEqualTo(generatedToken);
    }

    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequestDTO registerRequest = Instancio.create(RegisterRequestDTO.class);
        when(usersService.existByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Account with this email already exists");

        verify(passwordEncoder, never()).encode(anyString());
        verify(usersService, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }

}

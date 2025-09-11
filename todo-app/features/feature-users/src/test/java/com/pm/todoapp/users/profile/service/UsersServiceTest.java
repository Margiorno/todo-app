package com.pm.todoapp.users.profile.service;

import com.pm.todoapp.common.exceptions.UserNotFoundException;
import com.pm.todoapp.domain.file.port.FileStoragePort;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private UsersService usersService;

    private User user;

    @BeforeEach
    void setUp() {
        user = Instancio.create(User.class);
    }

    @Test
    void ensureUserExistsById_shouldNotThrowException_whenUserExists() {
        when(usersRepository.existsById(user.getId())).thenReturn(true);

        assertThatCode(() -> usersService.ensureUserExistsById(user.getId()))
                .doesNotThrowAnyException();

        verify(usersRepository).existsById(user.getId());
    }

    @Test
    void ensureUserExistsById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(usersRepository.existsById(user.getId())).thenReturn(false);

        assertThatThrownBy(() -> usersService.ensureUserExistsById(user.getId()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with this id does not exist: " + user.getId());
    }

}

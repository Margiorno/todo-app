package com.pm.todoapp.users.profile.service;

import com.pm.todoapp.common.exceptions.InvalidFieldException;
import com.pm.todoapp.common.exceptions.StorageException;
import com.pm.todoapp.common.exceptions.UserNotFoundException;
import com.pm.todoapp.domain.file.dto.FileType;
import com.pm.todoapp.domain.file.port.FileStoragePort;
import com.pm.todoapp.domain.user.model.Gender;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

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

    @Test
    void findRawById_shouldReturnUser_whenUserExists() {
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = usersService.findRawById(user.getId());

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = usersService.findByEmail(user.getEmail());

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByEmail_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(usersRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usersService.findByEmail(user.getEmail()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with this email does not exist: " + user.getEmail());
    }

    @Test
    void updateProfilePicture_shouldSaveNewPictureAndDeleteOldOne() throws IOException {
        String oldPicturePath = Instancio.create(String.class);
        String newPicturePath = Instancio.create(String.class);
        user.setProfilePicturePath(oldPicturePath);
        MockMultipartFile file = Instancio.create(MockMultipartFile.class);

        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(fileStoragePort.saveFile(any(InputStream.class), eq(file.getOriginalFilename()), eq(FileType.PROFILE_PICTURE)))
                .thenReturn(newPicturePath);

        String resultPath = usersService.updateProfilePicture(file, user.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(resultPath).isEqualTo(newPicturePath);
        assertThat(savedUser.getProfilePicturePath()).isEqualTo(newPicturePath);

        verify(fileStoragePort).saveFile(any(InputStream.class), eq(file.getOriginalFilename()), eq(FileType.PROFILE_PICTURE));
        verify(fileStoragePort).deleteFile(oldPicturePath, FileType.PROFILE_PICTURE);
    }

    @Test
    void updateProfilePicture_shouldThrowStorageException_whenFileReadFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("Test exception"));
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> usersService.updateProfilePicture(file, user.getId()))
                .isInstanceOf(StorageException.class)
                .hasMessage("Failed to read uploaded profile picture for user: " + user.getId());

        verify(usersRepository, never()).save(any(User.class));
        verify(fileStoragePort, never()).deleteFile(anyString(), any(FileType.class));
    }

    @Test
    void update_shouldUpdateFirstNameAndSaveChanges() {
        String newFirstName = Instancio.create(String.class);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(usersRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = usersService.update("firstName", newFirstName, user.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getFirstName()).isEqualTo(newFirstName);
        assertThat(result).containsEntry("firstName", newFirstName);
    }

    @Test
    void update_shouldUpdateDateOfBirthAndSaveChanges() {
        String newDateString = Instancio.create(LocalDate.class).toString();
        LocalDate newDate = LocalDate.parse(newDateString);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(usersRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = usersService.update("dateOfBirth", newDateString, user.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getDateOfBirth()).isEqualTo(newDate);
        assertThat(result).containsEntry("dateOfBirth", newDate);
    }

    @Test
    void update_shouldUpdateGenderAndSaveChanges() {
        String newGenderString = Instancio.create(Gender.class).toString();
        Gender newGender = Gender.valueOf(newGenderString);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(usersRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> result = usersService.update("gender", newGenderString, user.getId());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getGender()).isEqualTo(newGender);
        assertThat(result).containsEntry("gender", newGender);
    }

    @Test
    void update_shouldThrowInvalidFieldException_forUnsupportedField() {
        String invalidField = Instancio.create(String.class);
        when(usersRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> usersService.update(invalidField, "brown", user.getId()))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessage("Unsupported field: " + invalidField);

        verify(usersRepository, never()).save(any(User.class));
    }

}

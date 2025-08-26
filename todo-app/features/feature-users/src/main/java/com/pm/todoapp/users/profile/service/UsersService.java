package com.pm.todoapp.users.profile.service;

import com.pm.todoapp.common.exceptions.InvalidFieldException;
import com.pm.todoapp.common.exceptions.UserNotFoundException;
import com.pm.todoapp.domain.file.dto.FileType;
import com.pm.todoapp.domain.file.port.FileStoragePort;
import com.pm.todoapp.domain.user.model.Gender;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.profile.mapper.UserMapper;
import com.pm.todoapp.users.profile.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final FileStoragePort fileStoragePort;

    public void ensureUserExistsById(UUID userId) {
        if (!usersRepository.existsById(userId))
            throw new UserNotFoundException("User with this id does not exist: " + userId.toString());

    }

    public User findRawById(UUID userId) {
        return usersRepository.findById(userId).orElseThrow(
                ()->new UserNotFoundException("User with this id does not exist: " + userId.toString())
        );
    }

    public UserResponseDTO findById(UUID userId) {
        return UserMapper.toUserResponseDTO(findRawById(userId));
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public User findByEmail(String email) {
        return usersRepository.findByEmail(email).orElseThrow(
                ()-> new UserNotFoundException("User with this email does not exist: " + email)
        );
    }

    public boolean existByEmail(String email) {
        return usersRepository.existsByEmail(email);
    }

    @Transactional
    public String updateProfilePicture(MultipartFile file, UUID userId) {

        User user = findRawById(userId);
        String oldPicturePath = user.getProfilePicturePath();

        try {
            InputStream fileContent = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();

            String newPicturePath = fileStoragePort.saveFile(
                    fileContent,
                    originalFilename,
                    contentType,
                    FileType.PROFILE_PICTURE
            );

            user.setProfilePicturePath(newPicturePath);
            usersRepository.save(user);

            if (oldPicturePath != null && !oldPicturePath.isEmpty() && !oldPicturePath.isBlank()) {
                fileStoragePort.deleteFile(oldPicturePath, FileType.PROFILE_PICTURE);
            }

            return newPicturePath;

        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e.getMessage());
        }


    }

    public Map<String, Object> update(String field, String value, UUID userId) {
        User user = findRawById(userId);

        Object updatedValue;

        switch (field) {
            case "firstName" -> updatedValue = updateFirstName(user, value);
            case "lastName" -> updatedValue = updateLastName(user, value);
            case "dateOfBirth" -> updatedValue = updateDateOfBirth(user, LocalDate.parse(value));
            case "gender" -> updatedValue = updateGender(user, Gender.valueOf(value));
            case "email" -> updatedValue = updateEmail(user, value);
            default -> throw new InvalidFieldException("Unsupported field: " + field);
        }

        return Collections.singletonMap(field, updatedValue);
    }


    private String updateEmail(User user, String value) {
        user.setEmail(value);
        return usersRepository.save(user).getEmail();
    }

    private String updateFirstName(User user, String value) {
        user.setFirstName(value);
        return usersRepository.save(user).getFirstName();
    }
    private String updateLastName(User user, String value) {
        user.setLastName(value);
        return usersRepository.save(user).getLastName();
    }
    private LocalDate updateDateOfBirth(User user, LocalDate parse) {
        user.setDateOfBirth(parse);
        return usersRepository.save(user).getDateOfBirth();
    }
    private Gender updateGender(User user, Gender gender) {
        user.setGender(gender);
        return usersRepository.save(user).getGender();
    }
}

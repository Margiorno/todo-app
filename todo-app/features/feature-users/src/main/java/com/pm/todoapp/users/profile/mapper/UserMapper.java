package com.pm.todoapp.users.profile.mapper;

import com.pm.todoapp.users.profile.dto.UserResponseDTO;
import com.pm.todoapp.users.profile.model.User;

import java.time.format.DateTimeFormatter;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .profilePicturePath(user.getProfilePicturePath())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .gender(user.getGender().toString().toLowerCase())
                .build();
    }
}

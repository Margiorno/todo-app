package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.model.User;

import java.time.LocalDate;
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

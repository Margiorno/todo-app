package com.pm.todoapp.chat.mapper;

import com.pm.todoapp.chat.dto.SenderDTO;
import com.pm.todoapp.core.user.dto.UserDTO;

import java.time.format.DateTimeFormatter;

public class SenderMapper {
    public static SenderDTO fromUserDtoToSenderDTO(UserDTO userDTO) {
        return SenderDTO.builder()
                .id(userDTO.getId().toString())
                //.email(userDTO.getEmail())
                .profilePicturePath(userDTO.getProfilePicturePath())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                //.dateOfBirth(userDTO.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                //.gender(userDTO.getGender().toString().toLowerCase())
                .build();
    }
}

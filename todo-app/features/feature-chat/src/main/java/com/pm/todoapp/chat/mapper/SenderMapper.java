package com.pm.todoapp.chat.mapper;

import com.pm.todoapp.chat.dto.SenderDTO;
import com.pm.todoapp.domain.user.dto.UserDTO;


public class SenderMapper {
    public static SenderDTO fromUserDtoToSenderDTO(UserDTO userDTO) {
        return SenderDTO.builder()
                .id(userDTO.getId().toString())
                .profilePicturePath(userDTO.getProfilePicturePath())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .build();
    }
}

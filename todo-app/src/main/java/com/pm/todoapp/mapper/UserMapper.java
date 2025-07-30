package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.model.User;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .build();
    }
}

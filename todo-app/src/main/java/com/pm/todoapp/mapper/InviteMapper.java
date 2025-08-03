package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.InviteResponseDTO;
import com.pm.todoapp.model.Invite;


public class InviteMapper {
    public static InviteResponseDTO toResponseDTO(Invite invite) {
        return InviteResponseDTO.builder()
                .code(invite.getCode())
                .build();
    }


}

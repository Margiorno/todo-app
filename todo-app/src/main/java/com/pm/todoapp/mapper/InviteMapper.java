package com.pm.todoapp.mapper;

import com.pm.todoapp.dto.TeamInviteResponseDTO;
import com.pm.todoapp.model.Invite;


public class InviteMapper {
    public static TeamInviteResponseDTO toResponseDTO(Invite invite) {
        return TeamInviteResponseDTO.builder()
                .code(invite.getCode())
                .build();
    }


}

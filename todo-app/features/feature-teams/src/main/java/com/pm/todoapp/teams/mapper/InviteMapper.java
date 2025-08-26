package com.pm.todoapp.teams.mapper;

import com.pm.todoapp.teams.dto.TeamInviteResponseDTO;
import com.pm.todoapp.domain.teams.model.Invite;


public class InviteMapper {
    public static TeamInviteResponseDTO toResponseDTO(Invite invite) {
        return TeamInviteResponseDTO.builder()
                .code(invite.getCode())
                .build();
    }


}

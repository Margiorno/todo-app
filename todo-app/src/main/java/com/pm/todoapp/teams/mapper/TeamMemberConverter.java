package com.pm.todoapp.teams.mapper;

import com.pm.todoapp.core.user.dto.UserDTO;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.teams.dto.TeamMemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TeamMemberConverter {

    private final UserValidationPort userValidationPort;
    private final UserProviderPort userProviderPort;

    @Autowired
    public TeamMemberConverter(UserValidationPort userValidationPort, UserProviderPort userProviderPort) {
        this.userValidationPort = userValidationPort;
        this.userProviderPort = userProviderPort;
    }

    public TeamMemberDTO toDTO(UUID userId) {
        userValidationPort.ensureUserExistsById(userId);
        UserDTO userDTO = userProviderPort.getUserById(userId);

        return TeamMemberDTO.builder().id(userId).email(userDTO.getEmail()).build();
    }
}

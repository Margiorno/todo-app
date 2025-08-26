package com.pm.todoapp.teams.adapter;

import com.pm.todoapp.domain.teams.port.TeamValidationPort;
import com.pm.todoapp.teams.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TeamValidationAdapter implements TeamValidationPort {
    private final TeamService teamService;

    @Override
    public void ensureTeamExistsById(UUID teamId) {
        teamService.ensureTeamExistsById(teamId);
    }
}

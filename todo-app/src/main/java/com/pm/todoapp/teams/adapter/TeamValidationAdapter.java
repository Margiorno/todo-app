package com.pm.todoapp.teams.adapter;

import com.pm.todoapp.core.team.port.TeamValidationPort;
import com.pm.todoapp.teams.service.TeamService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TeamValidationAdapter implements TeamValidationPort {
    private final TeamService teamService;

    public TeamValidationAdapter(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public void ensureTeamExistsById(UUID teamId) {
        teamService.ensureTeamExistsById(teamId);
    }
}

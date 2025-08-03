package com.pm.todoapp.service;

import com.pm.todoapp.dto.TeamResponseDTO;
import com.pm.todoapp.exceptions.TeamNotFoundException;
import com.pm.todoapp.mapper.TeamMapper;
import com.pm.todoapp.model.Team;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final UsersService usersService;

    @Autowired
    public TeamService(TeamRepository teamRepository, UsersService usersService) {
        this.teamRepository = teamRepository;
        this.usersService = usersService;
    }

    // needed in task service
    public Team findById(UUID teamId) {
        return teamRepository.findById(teamId).
                orElseThrow(()->new TeamNotFoundException("Team with this id does not exist: " + teamId));
    }

    public List<TeamResponseDTO> findAll() {
        Iterable<Team> teams = teamRepository.findAll();

        return StreamSupport.stream(teams.spliterator(), false).map(TeamMapper::toResponseDTO).toList();
    }

    public List<TeamResponseDTO> findAllByUserId(UUID userId) {

        User user = usersService.findById(userId);
        Iterable<Team> teams = teamRepository.findByMembersContaining(user);

        return StreamSupport.stream(teams.spliterator(), false).map(TeamMapper::toResponseDTO).toList();
    }

    public void createTeam(String teamName, UUID userId) {

        User user = usersService.findById(userId);

        Team team = new Team();
        team.setName(teamName);
        team.getMembers().add(user);
        user.getTeams().add(team);

        teamRepository.save(team);
    }

}

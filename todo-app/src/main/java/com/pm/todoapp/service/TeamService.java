package com.pm.todoapp.service;

import com.pm.todoapp.exceptions.TeamNotFoundException;
import com.pm.todoapp.model.Team;
import com.pm.todoapp.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team findById(UUID teamId) {
        return teamRepository.findById(teamId).
                orElseThrow(()->new TeamNotFoundException("Team with this id does not exist: " + teamId));
    }

    //TODO TO REBUILD
    public List<Team> findAll() {
        return StreamSupport.stream(teamRepository.findAll().spliterator(), false).toList();
    }

}

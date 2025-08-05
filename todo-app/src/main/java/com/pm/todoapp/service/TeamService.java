package com.pm.todoapp.service;

import com.pm.todoapp.dto.InviteResponseDTO;
import com.pm.todoapp.dto.TeamResponseDTO;
import com.pm.todoapp.dto.UserResponseDTO;
import com.pm.todoapp.exceptions.InvalidInviteException;
import com.pm.todoapp.exceptions.TeamNotFoundException;
import com.pm.todoapp.mapper.InviteMapper;
import com.pm.todoapp.mapper.TeamMapper;
import com.pm.todoapp.mapper.UserMapper;
import com.pm.todoapp.model.Team;
import com.pm.todoapp.model.Invite;
import com.pm.todoapp.model.User;
import com.pm.todoapp.repository.InviteRepository;
import com.pm.todoapp.repository.TeamRepository;
import com.pm.todoapp.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final UsersService usersService;
    private final InviteRepository inviteRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, UsersService usersService, InviteRepository inviteRepository, UsersRepository usersRepository) {
        this.teamRepository = teamRepository;
        this.usersService = usersService;
        this.inviteRepository = inviteRepository;
    }

    // needed in task service
    public Team findById(UUID teamId) {

        if (teamId == null) {
            throw new TeamNotFoundException("Team not found");
        }

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
        usersService.save(user);
    }

    public List<UserResponseDTO> findUsersByTeamId(UUID teamId) {
        Team team = findById(teamId);

        return team.getMembers().stream().map(UserMapper::toUserResponseDTO).toList();
    }

    public InviteResponseDTO generateInviteCode(UUID teamId) {
        Team team = findById(teamId);

        Invite invite = new Invite();
        invite.setTeam(team);
        invite.setCode(generateInvitationCode());
        invite.setExpirationDate(LocalDateTime.now().plusMinutes(5));

        Invite savedInvite = inviteRepository.save(invite);

        return InviteMapper.toResponseDTO(savedInvite);
    }



    public void join(String code, UUID userId) {

        User user = usersService.findById(userId);
        Invite invite = inviteRepository.findByCode(code).orElseThrow(
                ()->new InvalidInviteException("There is no invitation with this code")
        );
        Team team = invite.getTeam();

        if (team.getMembers().contains(user))
            throw new InvalidInviteException("You are already in a member of this team");

        team.getMembers().add(user);
        user.getTeams().add(team);

        teamRepository.save(team);
        inviteRepository.delete(invite);
    }

    private String generateInvitationCode() {
        String generatedCode;
        do{
            byte[] array = new byte[6];
            new Random().nextBytes(array);
            RandomStringUtils generator = RandomStringUtils.insecure();
            generatedCode = generator.next(6, 'A', 'Z' + 1, false, false);
        } while (inviteRepository.existsByCode(generatedCode));

        return generatedCode;
    }

    public void deleteUserFromTeam(UUID teamId, UUID userId) {

        Team team = findById(teamId);
        User user = usersService.findById(userId);

        team.getMembers().remove(user);
        user.getTeams().remove(team);

        teamRepository.save(team);
        usersService.save(user);
    }
}

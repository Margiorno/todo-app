package com.pm.todoapp.teams.service;

import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.core.user.port.UserProviderPort;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.teams.dto.TeamInviteResponseDTO;
import com.pm.todoapp.teams.dto.TeamMemberDTO;
import com.pm.todoapp.teams.dto.TeamResponseDTO;
import com.pm.todoapp.core.exceptions.InvalidTeamInviteException;
import com.pm.todoapp.core.exceptions.TeamNotFoundException;
import com.pm.todoapp.teams.mapper.InviteMapper;
import com.pm.todoapp.teams.mapper.TeamMapper;
import com.pm.todoapp.teams.mapper.TeamMemberConverter;
import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.teams.model.Invite;
import com.pm.todoapp.teams.repository.TeamInviteRepository;
import com.pm.todoapp.teams.repository.TeamRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamInviteRepository inviteRepository;

    private final UserValidationPort userValidationPort;
    private final UserProviderPort userProviderPort;
    private final UserRepository userRepository;
    private final TeamMemberConverter teamMemberConverter;

    @Autowired
    public TeamService(TeamRepository teamRepository, TeamInviteRepository inviteRepository, UserValidationPort userValidationPort, UserProviderPort userProviderPort, UserRepository userRepository, TeamMemberConverter teamMemberConverter) {
        this.teamRepository = teamRepository;
        this.inviteRepository = inviteRepository;
        this.userValidationPort = userValidationPort;
        this.userProviderPort = userProviderPort;
        this.userRepository = userRepository;
        this.teamMemberConverter = teamMemberConverter;
    }

    public Team findRawById(UUID teamId) {

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

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Team> teams = teamRepository.findByMembersContaining(user);

        return StreamSupport.stream(teams.spliterator(), false).map(TeamMapper::toResponseDTO).toList();
    }

    public void createTeam(String teamName, UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Team team = new Team();
        team.setName(teamName);
        team.getMembers().add(user);

        teamRepository.save(team);
    }

    public TeamInviteResponseDTO generateInviteCode(UUID teamId) {
        Team team = findRawById(teamId);

        Invite invite = new Invite();
        invite.setTeam(team);
        invite.setCode(generateInvitationCode());
        invite.setExpirationDate(LocalDateTime.now().plusMinutes(5));

        Invite savedInvite = inviteRepository.save(invite);

        return InviteMapper.toResponseDTO(savedInvite);
    }



    public void join(String code, UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Invite invite = inviteRepository.findByCode(code).orElseThrow(
                ()->new InvalidTeamInviteException("There is no invitation with this code")
        );
        Team team = invite.getTeam();

        if (team.getMembers().contains(user))
            throw new InvalidTeamInviteException("You are already in a member of this team");

        team.getMembers().add(user);
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

        Team team = findRawById(teamId);

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        team.getMembers().remove(user);
        teamRepository.save(team);
    }

    public void ensureTeamExistsById(UUID teamId) {
        if(!teamRepository.existsById(teamId))
                throw new TeamNotFoundException("Team with this id does not exist: " + teamId);
    }

    public Set<TeamMemberDTO> findTeamMembers(UUID teamId) {
        Team team = findRawById(teamId);

        Set<User> members = team.getMembers();

        if (members.isEmpty()) {
            //TODO
        }

         return  members.stream()
                .map(User::getId)
                .map(teamMemberConverter::toDTO)
                .collect(Collectors.toSet());
    }
}

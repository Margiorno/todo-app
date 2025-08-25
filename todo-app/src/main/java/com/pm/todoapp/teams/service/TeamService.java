package com.pm.todoapp.teams.service;

import com.pm.todoapp.core.user.dto.UserDTO;
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
import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.teams.model.Invite;
import com.pm.todoapp.teams.repository.TeamInviteRepository;
import com.pm.todoapp.teams.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserValidationPort userValidationPort;
    private final UserRepository userRepository;
    private final UserProviderPort userProviderPort;
    private final InviteCodeService inviteCodeService;

    public Team findRawById(UUID teamId) {

        if (teamId == null) {
            throw new TeamNotFoundException("Team not found");
        }

        return teamRepository.findById(teamId).
                orElseThrow(()->new TeamNotFoundException("Team with this id does not exist: " + teamId));
    }


    public List<TeamResponseDTO> findAllByUserId(UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Team> teams = teamRepository.findByMembersContaining(user);

        return StreamSupport.stream(teams.spliterator(), false).map(TeamMapper::toResponseDTO).toList();
    }

    public TeamResponseDTO createTeam(String teamName, UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Team team = new Team();
        team.setName(teamName);
        team.getMembers().add(user);

        return TeamMapper.toResponseDTO(teamRepository.save(team));

    }

    public TeamInviteResponseDTO generateInviteCode(UUID teamId) {
        Team team = findRawById(teamId);
        Invite invite = inviteCodeService.createAndSaveInvite(team);
        return InviteMapper.toResponseDTO(invite);
    }

    @Transactional
    public void join(String code, UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Team team = inviteCodeService.resolveInvitationCode(code);

        if (team.getMembers().contains(user))
            throw new InvalidTeamInviteException("You are already in a member of this team");

        team.getMembers().add(user);
        teamRepository.save(team);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public Set<TeamMemberDTO> findTeamMembers(UUID teamId) {

        Team team = findRawById(teamId);
        Set<User> members = team.getMembers();

        if (members.isEmpty())
            return Collections.emptySet();

        Set<UUID> memberIds = members.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<UserDTO> usersDTOs = userProviderPort.getUsersByIds(memberIds);

        return usersDTOs.stream()
                .map(userDto -> new TeamMemberDTO(userDto.getId(), userDto.getEmail()))
                .collect(Collectors.toSet());
    }
}

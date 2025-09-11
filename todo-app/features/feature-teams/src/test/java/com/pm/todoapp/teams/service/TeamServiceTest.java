package com.pm.todoapp.teams.service;

import com.pm.todoapp.common.exceptions.InvalidTeamInviteException;
import com.pm.todoapp.common.exceptions.TeamNotFoundException;
import com.pm.todoapp.domain.user.dto.UserDTO;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserProviderPort;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.teams.dto.TeamInviteResponseDTO;
import com.pm.todoapp.teams.dto.TeamMemberDTO;
import com.pm.todoapp.teams.dto.TeamResponseDTO;
import com.pm.todoapp.teams.model.Invite;
import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.teams.repository.TeamRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private UserValidationPort userValidationPort;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProviderPort userProviderPort;
    @Mock
    private InviteCodeService inviteCodeService;

    @InjectMocks
    private TeamService teamService;

    private User user;
    private Team team;

    @BeforeEach
    void setUp() {
        user = Instancio.create(User.class);
        team = Instancio.of(Team.class)
                .set(field(Team::getMembers),Set.of(user))
                .create();
    }

    @Test
    void findRawById_shouldThrowException_whenIdIsNull() {
        assertThatThrownBy(() -> teamService.findRawById(null))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team not found");
    }

    @Test
    void findRawById_shouldThrowException_whenTeamDoesNotExist() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.findRawById(team.getId()))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team with this id does not exist: " + team.getId());
    }

    @Test
    void findRawById_shouldReturnTeam_whenTeamExists() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        Team result = teamService.findRawById(team.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(team.getId());
    }


}
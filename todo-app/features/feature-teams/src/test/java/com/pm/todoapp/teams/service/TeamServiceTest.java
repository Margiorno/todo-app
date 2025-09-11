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
                .set(field(Team::getMembers),new HashSet<>(Set.of(user)))
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

    @Test
    void findAllByUserId_shouldReturnTeamDTOs() {
        doNothing().when(userValidationPort).ensureUserExistsById(user.getId());
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(teamRepository.findByMembersContaining(user)).thenReturn(List.of(team));

        List<TeamResponseDTO> result = teamService.findAllByUserId(user.getId());

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(team.getId().toString());
        assertThat(result.getFirst().getName()).isEqualTo(team.getName());
    }

    @Test
    void createTeam_shouldCreateAndSaveTeam_andReturnDTO() {
        String teamName = Instancio.create(String.class);

        doNothing().when(userValidationPort).ensureUserExistsById(user.getId());
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team savedTeam = invocation.getArgument(0);
            savedTeam.setId(UUID.randomUUID());
            return savedTeam;
        });

        TeamResponseDTO result = teamService.createTeam(teamName, user.getId());

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team capturedTeam = teamCaptor.getValue();

        assertThat(capturedTeam.getName()).isEqualTo(teamName);
        assertThat(capturedTeam.getMembers()).containsExactly(user);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(teamName);
    }

    @Test
    void generateInviteCode_shouldCreateAndReturnInviteDTO() {
        Invite invite = Instancio.create(Invite.class);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(inviteCodeService.createAndSaveInvite(team)).thenReturn(invite);

        TeamInviteResponseDTO result = teamService.generateInviteCode(team.getId());

        verify(inviteCodeService).createAndSaveInvite(team);
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(invite.getCode());
    }

    @Test
    void join_shouldAddUserToTeam_whenCodeIsValid() {
        String validCode = Instancio.create(String.class);
        User newUser = Instancio.create(User.class);

        doNothing().when(userValidationPort).ensureUserExistsById(newUser.getId());
        when(userRepository.getReferenceById(newUser.getId())).thenReturn(newUser);
        when(inviteCodeService.resolveInvitationCode(validCode)).thenReturn(team);

        teamService.join(validCode, newUser.getId());

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team savedTeam = teamCaptor.getValue();

        assertThat(savedTeam.getMembers()).contains(user, newUser);
    }

    @Test
    void join_shouldThrowException_whenUserIsAlreadyMember() {
        String validCode = Instancio.create(String.class);

        doNothing().when(userValidationPort).ensureUserExistsById(user.getId());
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);
        when(inviteCodeService.resolveInvitationCode(validCode)).thenReturn(team);

        assertThatThrownBy(() -> teamService.join(validCode, user.getId()))
                .isInstanceOf(InvalidTeamInviteException.class)
                .hasMessage("You are already in a member of this team");

        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    void deleteUserFromTeam_shouldRemoveUserAndSaveChanges() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        doNothing().when(userValidationPort).ensureUserExistsById(user.getId());
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        teamService.deleteUserFromTeam(team.getId(), user.getId());

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team savedTeam = teamCaptor.getValue();

        assertThat(savedTeam.getMembers()).doesNotContain(user);
    }

    @Test
    void ensureTeamExistsById_shouldThrowException_whenTeamDoesNotExist() {
        when(teamRepository.existsById(team.getId())).thenReturn(false);

        assertThatThrownBy(() -> teamService.ensureTeamExistsById(team.getId()))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team with this id does not exist: " + team.getId());
    }

    @Test
    void ensureTeamExistsById_shouldNotThrowException_whenTeamExists() {
        when(teamRepository.existsById(team.getId())).thenReturn(true);

        assertThatCode(() -> teamService.ensureTeamExistsById(team.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void findTeamMembers_shouldReturnMappedMemberDTOs() {
        User user2 = Instancio.create(User.class);
        team.getMembers().add(user2);

        UserDTO user1DTO = Instancio.of(UserDTO.class)
                .set(field(UserDTO::getId),user.getId())
                .create();
        UserDTO user2DTO = Instancio.of(UserDTO.class)
                .set(field(UserDTO::getId),user2.getId())
                .create();

        Set<UUID> memberIds = Set.of(user.getId(), user2.getId());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userProviderPort.getUsersByIds(memberIds)).thenReturn(Set.of(user1DTO, user2DTO));

        Set<TeamMemberDTO> result = teamService.findTeamMembers(team.getId());

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting("id", "firstName", "lastName", "email")
                .containsExactlyInAnyOrder(
                        tuple(user1DTO.getId(), user1DTO.getFirstName(), user1DTO.getLastName(), user1DTO.getEmail()),
                        tuple(user2DTO.getId(), user2DTO.getFirstName(), user2DTO.getLastName(), user2DTO.getEmail())
                );
    }
}
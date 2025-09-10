package com.pm.todoapp.tasks.service;

import com.pm.todoapp.common.exceptions.TaskAccessDeniedException;
import com.pm.todoapp.domain.teams.model.Team;
import com.pm.todoapp.domain.teams.port.TeamValidationPort;
import com.pm.todoapp.domain.teams.repository.TeamRepository;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.domain.user.port.UserValidationPort;
import com.pm.todoapp.domain.user.repository.UserRepository;
import com.pm.todoapp.tasks.model.Task;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TaskValidationServiceTest {
    @Mock
    private TeamValidationPort teamValidationPort;
    @Mock
    private UserValidationPort userValidationPort;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskValidationService taskValidationService;

    @Test
    public void shouldNotThrowExceptionWhenUserIsAssignedToTask(){
        User user = Instancio.create(User.class);
        Task task = Instancio.of(Task.class)
                .set(field(Task::getAssignees), Set.of(user))
                .create();

        assertDoesNotThrow(() -> taskValidationService.validateUserAssignedToTask(task, user));
    }

    @Test
    public void shouldThrowExceptionWhenUserIsNotAssignedToTask(){
        User user = Instancio.create(User.class);
        Task task = Instancio.of(Task.class).create();

        TaskAccessDeniedException exception = assertThrows(TaskAccessDeniedException.class,
                () -> taskValidationService.validateUserAssignedToTask(task, user)
        );

        String expectedMessage = "User '%s' is not authorized to modify task '%s'"
                .formatted(user.getId(), task.getId());

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void shouldNotThrowExceptionWhenTeamIsAssignedToTask(){
        Team team = Instancio.create(Team.class);
        Task task = Instancio.of(Task.class)
                .set(field(Task::getTeam), team)
                .create();

        assertDoesNotThrow(() -> taskValidationService.validateTeamMatches(task, team.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenTeamIsNotAssignedToTask(){
        Team team = Instancio.create(Team.class);
        Task task = Instancio.create(Task.class);

        TaskAccessDeniedException exception = assertThrows(TaskAccessDeniedException.class,
                () -> taskValidationService.validateTeamMatches(task, team.getId())
        );
    }
}

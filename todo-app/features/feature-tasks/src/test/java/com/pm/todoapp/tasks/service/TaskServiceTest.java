package com.pm.todoapp.tasks.service;

import com.pm.todoapp.common.exceptions.TaskNotFoundException;
import com.pm.todoapp.domain.teams.model.Team;
import com.pm.todoapp.domain.user.model.User;
import com.pm.todoapp.tasks.dto.TaskFetchScope;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.tasks.mapper.TaskMapper;
import com.pm.todoapp.tasks.model.Priority;
import com.pm.todoapp.tasks.model.Status;
import com.pm.todoapp.tasks.model.Task;
import com.pm.todoapp.tasks.repository.TaskDAO;
import com.pm.todoapp.tasks.repository.TaskRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskDAO taskDAO;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskValidationService taskValidationService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Team team;
    private Task task;

    @BeforeEach
    void setUp() {
        user = Instancio.create(User.class);
        team = Instancio.create(Team.class);
        task = Instancio.create(Task.class);
    }

    @Test
    void save_shouldCreatePersonalTask_whenTeamIdIsNull() {
        TaskResponseDTO taskResponseDTO = Instancio.create(TaskResponseDTO.class);
        TaskRequestDTO taskRequestDTO = Instancio.create(TaskRequestDTO.class);

        when(taskValidationService.getValidatedUser(user.getId())).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.save(taskRequestDTO, user.getId(), null);

        verify(taskValidationService).getValidatedUser(user.getId());
        verify(taskValidationService, never()).getValidatedTeam(any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result).isEqualTo(taskResponseDTO);
    }

    @Test
    void save_shouldCreateTeamTask_whenTeamIdIsProvided() {
        TaskResponseDTO taskResponseDTO = Instancio.create(TaskResponseDTO.class);
        TaskRequestDTO taskRequestDTO = Instancio.create(TaskRequestDTO.class);

        when(taskValidationService.getValidatedUser(user.getId())).thenReturn(user);
        when(taskValidationService.getValidatedTeam(team.getId())).thenReturn(team);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.save(taskRequestDTO, user.getId(), team.getId());

        verify(taskValidationService).getValidatedUser(user.getId());
        verify(taskValidationService).getValidatedTeam(team.getId());
        verify(taskRepository).save(any(Task.class));
        assertThat(result).isEqualTo(taskResponseDTO);
    }

    @Test
    void update_shouldUpdateTask_whenValidationsPass() {
        TaskResponseDTO taskResponseDTO = Instancio.create(TaskResponseDTO.class);
        TaskRequestDTO taskRequestDTO = Instancio.create(TaskRequestDTO.class);

        when(taskValidationService.getValidatedUser(user.getId())).thenReturn(user);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        doNothing().when(taskValidationService).validateUserAssignedToTask(task, user);
        doNothing().when(taskValidationService).validateTeamMatches(task, team.getId());
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponseDTO(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.update(taskRequestDTO, task.getId(), user.getId(), team.getId());

        verify(taskRepository).findById(task.getId());
        verify(taskValidationService).validateUserAssignedToTask(task, user);
        verify(taskValidationService).validateTeamMatches(task, team.getId());
        verify(taskRepository).save(any(Task.class));
        assertThat(result).isEqualTo(taskResponseDTO);
    }

    @Test
    void update_shouldThrowException_whenTaskNotFound() {
        TaskRequestDTO taskRequestDTO = Instancio.create(TaskRequestDTO.class);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(taskRequestDTO, task.getId(), user.getId(), team.getId()))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with this id does not exists: %s".formatted(task.getId()));
    }
}

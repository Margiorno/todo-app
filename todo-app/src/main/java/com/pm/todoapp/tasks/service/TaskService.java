package com.pm.todoapp.tasks.service;

import com.pm.todoapp.core.team.model.Team;
import com.pm.todoapp.core.team.port.TeamValidationPort;
import com.pm.todoapp.core.team.repository.TeamRepository;
import com.pm.todoapp.core.user.model.User;
import com.pm.todoapp.core.user.port.UserValidationPort;
import com.pm.todoapp.core.user.repository.UserRepository;
import com.pm.todoapp.tasks.dto.TaskFetchScope;
import com.pm.todoapp.tasks.dto.TaskRequestDTO;
import com.pm.todoapp.tasks.dto.TaskResponseDTO;
import com.pm.todoapp.core.exceptions.TaskAccessDeniedException;
import com.pm.todoapp.core.exceptions.TaskNotFoundException;
import com.pm.todoapp.core.exceptions.TeamRequiredException;
import com.pm.todoapp.core.exceptions.UserRequiredException;
import com.pm.todoapp.tasks.mapper.TaskMapper;
import com.pm.todoapp.tasks.model.Priority;
import com.pm.todoapp.tasks.model.Status;
import com.pm.todoapp.tasks.model.Task;
import com.pm.todoapp.tasks.repository.TaskDAO;
import com.pm.todoapp.tasks.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskDAO taskDAO;
    private final UserValidationPort userValidationPort;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TeamRepository teamRepository;
    private final TeamValidationPort teamValidationPort;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO,
                       UserValidationPort userValidationPort, UserRepository userRepository, TaskMapper taskMapper, TeamRepository teamRepository, TeamValidationPort teamValidationPort) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
        this.userValidationPort = userValidationPort;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.teamRepository = teamRepository;
        this.teamValidationPort = teamValidationPort;
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto, UUID userId, UUID teamId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Task task = TaskMapper.toEntity(taskDto, Set.of(user));

        if (teamId != null) {
            teamValidationPort.ensureTeamExistsById(teamId);
            Team team = teamRepository.getReferenceById(teamId);
            task.setTeam(team);
        }

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO update(TaskRequestDTO taskDto, UUID taskId, UUID userId, UUID teamId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Task fromDb = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task with this id does not exists: %s".formatted(taskId))
        );

        if (!fromDb.getAssignees().contains(user)) {
            throw new TaskAccessDeniedException("User '%s' is not authorized to modify task '%s'".formatted(userId, taskId));
        }

        UUID existingTeamId = fromDb.getTeam() != null ? fromDb.getTeam().getId() : null;

        if (!Objects.equals(existingTeamId, teamId)) {
            throw new TaskAccessDeniedException("Cannot change team of the task '%s'".formatted(taskId));
        }

        Task task = TaskMapper.toEntity(taskDto, fromDb.getAssignees(), taskId);
        task.setTeam(fromDb.getTeam());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }


    public List<TaskResponseDTO> findByUserId(UUID userId) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Task> tasks = taskRepository.findByAssigneesContaining(user);
        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO findByTaskId(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));

        return taskMapper.toResponseDTO(task);
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate, UUID userId, UUID teamId, TaskFetchScope taskFetchScope) {

        userValidationPort.ensureUserExistsById(userId);
        User user = userRepository.getReferenceById(userId);

        Iterable<Task> tasks = switch (teamId){
            case null -> taskRepository.findByAssigneesContainingAndTaskDate(user, centerDate);
            default -> {
                Team team = teamRepository.getReferenceById(teamId);
                yield switch (taskFetchScope){
                    case TEAM_TASKS -> taskRepository.findByTeamAndTaskDate(team, centerDate);
                    case USER_TASKS -> taskRepository.findByAssigneesContainingAndTeamAndTaskDate(user, team, centerDate);
                };
            }
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByTeam(UUID teamId, UUID userId, TaskFetchScope taskFetchScope) {

        userValidationPort.ensureUserExistsById(userId);
        teamValidationPort.ensureTeamExistsById(teamId);

        User user = userRepository.getReferenceById(userId);
        Team team = teamRepository.getReferenceById(teamId);

        Iterable<Task> tasks;

        tasks = switch (taskFetchScope){
            case TEAM_TASKS -> taskRepository.findByTeam(team);
            case USER_TASKS -> taskRepository.findByAssigneesContainingAndTeam(user, team);
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByBasicFilters(
            Priority priority,
            Status status,
            LocalDate startDate,
            LocalDate endDate,
            UUID userId,
            UUID teamId,
            TaskFetchScope taskFetchScope
    ) {

        User user = null;
        Team team = null;

        switch (taskFetchScope) {
            case USER_TASKS:
                if (userId == null) {
                    throw new UserRequiredException("User ID is required when fetching user-specific tasks.");
                }
                userValidationPort.ensureUserExistsById(userId);
                user = userRepository.getReferenceById(userId);

                if (teamId != null) {
                    teamValidationPort.ensureTeamExistsById(teamId);
                    team = teamRepository.getReferenceById(teamId);
                }
                break;

            case TEAM_TASKS:
                if (teamId == null) {
                    throw new TeamRequiredException("Team ID is required when fetching tasks for an entire team.");
                }
                teamValidationPort.ensureTeamExistsById(teamId);
                team = teamRepository.getReferenceById(teamId);
                break;
        }

        Iterable<Task> tasks = taskDAO.findByBasicFilters(priority, status, startDate, endDate, user, team);

        return StreamSupport.stream(tasks.spliterator(), false).map(taskMapper::toResponseDTO).toList();
    }

    public TaskRequestDTO findTaskRequestById(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new TaskNotFoundException("Task with this id does not exists: %s".formatted(id))
        );

        return TaskMapper.toRequestDto(task);
    }
}

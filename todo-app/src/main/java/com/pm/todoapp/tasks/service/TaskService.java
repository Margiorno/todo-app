package com.pm.todoapp.tasks.service;

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
import com.pm.todoapp.teams.model.Team;
import com.pm.todoapp.teams.service.TeamService;
import com.pm.todoapp.users.profile.model.User;
import com.pm.todoapp.users.profile.service.UsersService;
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

    private final TeamService teamService;
    private final UsersService usersService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskDAO taskDAO, TeamService teamService, UsersService usersService) {
        this.taskRepository = taskRepository;
        this.taskDAO = taskDAO;
        this.teamService = teamService;
        this.usersService = usersService;
    }

    public TaskResponseDTO save(TaskRequestDTO taskDto, UUID userId, UUID teamId) {

        User user = usersService.findRawById(userId);
        Task task = TaskMapper.toEntity(taskDto, Set.of(user));

        if (teamId != null) {
            Team team = teamService.findRawById(teamId);
            task.setTeam(team);
        }

        Task savedTask = taskRepository.save(task);
        return TaskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO update(TaskRequestDTO taskDto, UUID taskId, UUID userId, UUID teamId) {

        User user = usersService.findRawById(userId);

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
        return TaskMapper.toResponseDTO(savedTask);
    }


    // FINDING
    public List<TaskResponseDTO> findByUserId(UUID userId) {
        User user = usersService.findRawById(userId);

        Iterable<Task> tasks = taskRepository.findByAssigneesContaining(user);
        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskResponseDTO findByTaskId(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                ()->new TaskNotFoundException("Task with this id does not exists: %s".formatted(id)));

        return TaskMapper.toResponseDTO(task);
    }

    public List<TaskResponseDTO> findByDate(LocalDate centerDate, UUID userId, UUID teamId, TaskFetchScope taskFetchScope) {

        User user = usersService.findRawById(userId);

        Iterable<Task> tasks = switch (teamId){
            case null -> taskRepository.findByAssigneesContainingAndTaskDate(user, centerDate);
            default -> {
                Team team = teamService.findRawById(teamId);
                yield switch (taskFetchScope){
                    case TEAM_TASKS -> taskRepository.findByTeamAndTaskDate(team, centerDate);
                    case USER_TASKS -> taskRepository.findByAssigneesContainingAndTeamAndTaskDate(user, team, centerDate);
                };
            }
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public List<TaskResponseDTO> findByTeam(UUID teamId, UUID userId, TaskFetchScope taskFetchScope) {

        User user = usersService.findRawById(userId);
        Team team = teamService.findRawById(teamId);

        Iterable<Task> tasks;

        tasks = switch (taskFetchScope){
            case TEAM_TASKS -> taskRepository.findByTeam(team);
            case USER_TASKS -> taskRepository.findByAssigneesContainingAndTeam(user, team);
        };

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
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
                user = usersService.findRawById(userId);

                if (teamId != null) {
                    team = teamService.findRawById(teamId);
                }
                break;

            case TEAM_TASKS:
                if (teamId == null) {
                    throw new TeamRequiredException("Team ID is required when fetching tasks for an entire team.");
                }
                team = teamService.findRawById(teamId);
                break;
        }

        Iterable<Task> tasks = taskDAO.findByBasicFilters(priority, status, startDate, endDate, user, team);

        return StreamSupport.stream(tasks.spliterator(), false).map(TaskMapper::toResponseDTO).toList();
    }

    public TaskRequestDTO findTaskRequestById(UUID id) {
        Task task = taskRepository.findById(id).orElseThrow(
                () -> new TaskNotFoundException("Task with this id does not exists: %s".formatted(id))
        );

        return TaskMapper.toRequestDto(task);
    }
}
